package framework;

import framework.api.Path;
import framework.api.QueryParam;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler((HttpServerExchange exchange) -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");

                    try {
                        Stream<Class<?>> classes = Stream.empty();

                        Stream<Method> methods = classes.flatMap(c -> Stream.of(c.getDeclaredMethods()));

                        Stream<Method> methodsWithPath = methods.filter(m -> m.getAnnotation(Path.class) != null);

                        Map<String, Method> methodByPath = methodsWithPath.collect(Collectors.toMap(
                                m -> m.getAnnotation(Path.class).value(),
                                m -> m
                        ));

                        Method method = methodByPath.get(exchange.getRequestPath());

                        Stream<Parameter> paramsStream = Stream.of(method.getParameters());

                        Object[] arguments = paramsStream.map(p -> {
                            QueryParam annotation = p.getAnnotation(QueryParam.class);
                            String paramName = annotation.value();
                            String paramValue = exchange.getQueryParameters().get(paramName).getFirst();

                            Class<?> paramType = p.getType();

                            if (paramType.equals(Double.class)) {
                                return Double.valueOf(paramValue);
                            } else {
                                return paramValue;
                            }

                        }).toArray();

                        Object o = method.invoke(method.getDeclaringClass().newInstance(), arguments);

                        String result = String.valueOf(o);

                        exchange.getResponseSender().send(result);


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
