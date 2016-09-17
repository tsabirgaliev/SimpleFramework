package framework;

import framework.api.Component;
import framework.api.Path;
import framework.api.QueryParam;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.Headers;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static Class<?> loadClass(String name, ClassLoader cl) {
        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static Object substituteParams(Parameter p, HttpServerExchange exchange) {
        String paramName = p.getAnnotation(QueryParam.class).value();
        String paramValue = exchange.getQueryParameters().get(paramName).getFirst();

        Class<?> paramType = p.getType();

        if (paramType.equals(Double.class)) {
            return Double.valueOf(paramValue);
        } else {
            return paramValue;
        }
    }

    static Object newInstance(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Cookie createSessionCookie(String cookieName) {
        return new CookieImpl(cookieName, UUID.randomUUID().toString());
    }

    public static void main(String[] args) throws Exception {
        Consumer println = System.out::println;

        File file = new File("application/build/libs/application.jar");

        ClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});

        JarFile jarFile = new JarFile(file);

        Map<String, Method> methodByPath = jarFile.stream()
                .map(entry -> entry.getName())
                .filter(name -> name.endsWith(".class"))
                .map(name -> name.replace('/', '.'))
                .map(name -> name.substring(0, name.length() - ".class".length()))
                .map(name -> loadClass(name,classLoader))
                .filter(clazz -> clazz.getAnnotation(Component.class) != null)
                .flatMap(c -> Stream.of(c.getDeclaredMethods()))
                .filter(m -> m.getAnnotation(Path.class) != null)
                .collect(Collectors.toMap(
                    m -> m.getAnnotation(Path.class).value(),
                    m -> m
                ));

        Map<Class<?>, Object> applicationScope = new HashMap<>();

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler((HttpServerExchange exchange) -> {
                    try {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");

                        Cookie sessionCookie = exchange.getRequestCookies()
                                .computeIfAbsent("sessionId", Main::createSessionCookie);

                        exchange.getResponseCookies().put("sessionId", sessionCookie);

                        String sessionId = sessionCookie.getValue();

                        Method method = methodByPath.get(exchange.getRequestPath());

                        Object[] arguments = Stream.of(method.getParameters())
                            .map(parameter -> substituteParams(parameter, exchange))
                            .toArray();

                        Class<?> clazz = method.getDeclaringClass();

                        Object instance = null;

                        switch (clazz.getAnnotation(Component.class).value() ) {
                            case APPLICATION:
                                instance = applicationScope.computeIfAbsent(clazz, Main::newInstance);
                                break;
                            case SESSION:
                                // TODO: Implement session scoped components
                                instance = clazz.newInstance();
                                break;
                            case REQUEST:
                                // TODO: Implement request scoped components
                                instance = clazz.newInstance();
                                break;
                            default:
                                instance = clazz.newInstance();
                                break;
                        }

                        Object result = method.invoke(instance, arguments);

                        exchange.getResponseSender().send(String.valueOf(result));


                    } catch (Exception e) {
                        e.printStackTrace();
                        StringWriter writer = new StringWriter();
                        e.printStackTrace(new PrintWriter(writer));
                        String errorMessage = writer.toString();
                        exchange.getResponseSender().send(errorMessage);
                    }

                }).build();
        server.start();
    }
}
