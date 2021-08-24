package de.mariushubatschek.is.scheduling.solving;

import de.mariushubatschek.is.scheduling.importing.ProblemData;
import de.mariushubatschek.is.scheduling.modeling.Plan;

public interface Solver {

    Plan solve(final ProblemData problemData);

}
