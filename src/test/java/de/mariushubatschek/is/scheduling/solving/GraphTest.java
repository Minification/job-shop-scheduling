package de.mariushubatschek.is.scheduling.solving;

import de.mariushubatschek.is.scheduling.importing.JobData;
import de.mariushubatschek.is.scheduling.importing.OperationData;
import de.mariushubatschek.is.scheduling.modeling.Graph;
import de.mariushubatschek.is.scheduling.modeling.Vertex;
import java.util.Arrays;
import java.util.Deque;
import org.junit.jupiter.api.Test;

class GraphTest {

    @Test
    public void testTopSort() {
        Graph graph = new Graph();

        JobData jobData1 = new JobData();
        jobData1.setId(1);
        OperationData operationData31 = new OperationData();
        operationData31.setIndex(0);
        operationData31.setDuration(4);
        operationData31.setResource(3);
        OperationData operationData21 = new OperationData();
        operationData21.setIndex(1);
        operationData21.setDuration(2);
        operationData21.setResource(2);
        OperationData operationData11 = new OperationData();
        operationData11.setIndex(2);
        operationData11.setDuration(1);
        operationData11.setResource(1);
        jobData1.setOperations(Arrays.asList(operationData31, operationData21, operationData11));

        JobData jobData2 = new JobData();
        jobData2.setId(2);
        OperationData operationData12 = new OperationData();
        operationData12.setIndex(0);
        operationData12.setDuration(3);
        operationData12.setResource(1);
        OperationData operationData32 = new OperationData();
        operationData32.setIndex(1);
        operationData32.setDuration(3);
        operationData32.setResource(3);
        jobData2.setOperations(Arrays.asList(operationData12, operationData32));

        JobData jobData3 = new JobData();
        jobData3.setId(3);
        OperationData operationData23 = new OperationData();
        operationData23.setIndex(0);
        operationData23.setDuration(2);
        operationData23.setResource(2);
        OperationData operationData13 = new OperationData();
        operationData13.setIndex(1);
        operationData13.setDuration(4);
        operationData13.setResource(1);
        OperationData operationData33 = new OperationData();
        operationData33.setIndex(2);
        operationData33.setDuration(1);
        operationData33.setResource(3);
        jobData3.setOperations(Arrays.asList(operationData23, operationData13, operationData33));

        graph.insertJob(jobData1);
        graph.insertJob(jobData2);
        graph.insertJob(jobData3);
        graph.insertDisjunctiveConstraints();

        System.out.println(graph);
        /*Deque<Vertex> sort = graph.sort();
        System.out.println(sort);

        graph.findDirections();

        System.out.println(graph);

        sort = graph.sort();
        System.out.println(sort);*/

        System.out.println(graph.makespan());
    }

    @Test
    public void testAgain() {
        Graph graph = new Graph();

        JobData jobData1 = new JobData();
        jobData1.setId(1);
        OperationData firstJob1 = new OperationData();
        firstJob1.setIndex(0);
        firstJob1.setDuration(3);
        firstJob1.setResource(0);
        OperationData secondJob1 = new OperationData();
        secondJob1.setIndex(1);
        secondJob1.setDuration(2);
        secondJob1.setResource(1);
        OperationData thirdJob1 = new OperationData();
        thirdJob1.setIndex(2);
        thirdJob1.setDuration(2);
        thirdJob1.setResource(2);
        jobData1.setOperations(Arrays.asList(firstJob1, secondJob1, thirdJob1));

        JobData jobData2 = new JobData();
        jobData2.setId(2);
        OperationData firstJob2 = new OperationData();
        firstJob2.setIndex(0);
        firstJob2.setDuration(2);
        firstJob2.setResource(0);
        OperationData secondJob2 = new OperationData();
        secondJob2.setIndex(1);
        secondJob2.setDuration(1);
        secondJob2.setResource(2);
        OperationData thirdJob2 = new OperationData();
        thirdJob2.setIndex(1);
        thirdJob2.setDuration(3);
        thirdJob2.setResource(1);
        jobData2.setOperations(Arrays.asList(firstJob2, secondJob2, thirdJob2));

        JobData jobData3 = new JobData();
        jobData3.setId(3);
        OperationData firstJob3 = new OperationData();
        firstJob3.setIndex(0);
        firstJob3.setDuration(4);
        firstJob3.setResource(1);
        OperationData secondJob3 = new OperationData();
        secondJob3.setIndex(1);
        secondJob3.setDuration(3);
        secondJob3.setResource(2);
        jobData3.setOperations(Arrays.asList(firstJob3, secondJob3));

        graph.insertJob(jobData1);
        graph.insertJob(jobData2);
        graph.insertJob(jobData3);
        graph.insertDisjunctiveConstraints();

        System.out.println(graph);
        /*Deque<Vertex> sort = graph.sort();
        System.out.println(sort);

        graph.findDirections();

        System.out.println(graph);

        sort = graph.sort();
        System.out.println(sort);*/

        System.out.println(graph.makespan());
    }

}