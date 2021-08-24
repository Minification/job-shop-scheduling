package de.mariushubatschek.is.scheduling.modeling;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OriginalPlan implements Plan {

    private final Map<Integer, List<Operation>> data;

    public OriginalPlan(final Map<Integer, List<Operation>> data) {
        this.data = data;
    }

    @Override
    public int makespan() {
        //Maximum end time that an operation has, plus 1 (since it's zero-based)
        return data.values()
            .stream()
            .flatMap(Collection::stream)
            .mapToInt(Operation::getEndTime)
            .max()
            .getAsInt() + 1;
    }

    @Override
    public boolean isValid() {
        for (List<Operation> operations : data.values()) {
            for (Operation operation : operations) {
                if (operation.getPreviousOperation() == null) {
                    continue;
                }
                if (operation.getStartTime() <= operation.getPreviousOperation().getEndTime()) {
                    return false;
                }
                /*System.out.println(operation
                    .getStartTime() - operation.getPreviousOperation().getEndTime());*/
            }
        }
        return true;
    }

    @Override
    public String toString() {
        //System.out.println(data);
        StringBuilder sb = new StringBuilder();
        for (final int resource : data.keySet()) {
            List<Operation> operations = data.get(resource);
            int lastEndTime = 0;
            sb.append(resource).append(" ");
            for (Operation operation : operations) {
                // As many spaces as there is empty space
                sb.append(" ".repeat(Math.max(0, operation.getStartTime() - lastEndTime - 1)));
                //-1 or -2 or something, I have no idea

                //As many dashes as there is duration
                sb.append(Character.toString(65 + operation.getJobIndex()).repeat(
                    operation.getDuration()));
                lastEndTime = operation.getEndTime();

            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public List<Plan> neighbourhood(final Random random) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
