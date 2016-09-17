package application;

import framework.api.Component;
import framework.api.Path;
import framework.api.Scope;

import java.util.concurrent.atomic.AtomicInteger;

@Component(Scope.APPLICATION)
public class Bye {

    AtomicInteger counter = new AtomicInteger(0);

    @Path("/bye")
    public String sayBye() {
        int newValue = counter.incrementAndGet();
        return newValue + " times Bye!";
    }
}
