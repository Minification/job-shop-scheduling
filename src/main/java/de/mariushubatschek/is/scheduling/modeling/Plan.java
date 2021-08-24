package de.mariushubatschek.is.scheduling.modeling;

import java.util.List;
import java.util.Random;

public interface Plan {

    int makespan();

    boolean isValid();

    List<Plan> neighbourhood(final Random random);
}
