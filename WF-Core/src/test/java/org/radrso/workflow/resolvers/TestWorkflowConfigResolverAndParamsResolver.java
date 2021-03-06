package org.radrso.workflow.resolvers;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.radrso.workflow.constant.ConfigConstant;
import org.radrso.workflow.entities.config.WorkflowConfig;
import org.radrso.workflow.entities.config.items.Step;
import org.radrso.workflow.entities.config.items.Transfer;
import org.radrso.workflow.entities.exceptions.ConfigReadException;
import org.radrso.workflow.entities.exceptions.UnknownExceptionInRunning;
import org.radrso.workflow.entities.response.WFResponse;
import org.radrso.workflow.entities.wf.StepStatus;
import org.radrso.workflow.entities.wf.WorkflowInstance;
import org.radrso.workflow.internal.resolver.ParamsResolverImpl;
import org.radrso.workflow.internal.resolver.FlowResolverImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rao-mengnan on 2017/3/10.
 */

public class TestWorkflowConfigResolverAndParamsResolver {
    FlowResolverImpl workflowResolver;
    WorkflowInstance workflowInstance;
    WorkflowConfig workflowConfig;
    ParamsResolverImpl paramsResolver;

    @Before
    public void before() {
        workflowConfig = new Gson().fromJson(wf, WorkflowConfig.class);
        workflowInstance = new WorkflowInstance("workflow-test", "instance-test");
        workflowResolver = new FlowResolverImpl(workflowConfig, workflowInstance);
        paramsResolver = new ParamsResolverImpl(workflowInstance);

    }

    @Test
    public void testGetParam() throws UnknownExceptionInRunning, ConfigReadException {

        Transfer transfer = workflowConfig.getSteps().get(0).getTransfer();
        paramsResolver.resolverTransferParams(transfer);

        StepStatus stepStatus_1 = workflowResolver.getWorkflowInstance().getStepStatusesMap().get("sign-1");
        Assert.assertEquals(stepStatus_1.getParams()[0].getClass(), String.class);
        Assert.assertEquals(stepStatus_1.getParams()[1].getClass(), Double.class);
        Assert.assertEquals(stepStatus_1.getParams()[2].getClass(), Integer.class);

        System.out.println("List.class:" + stepStatus_1.getParams()[3].getClass());
        Assert.assertEquals(stepStatus_1.getParams()[3].getClass(), ArrayList.class);

        System.out.println("int[].class:" + stepStatus_1.getParams()[4].getClass());
        Assert.assertEquals(stepStatus_1.getParams()[4].getClass(), int[].class);
        Assert.assertEquals(stepStatus_1.getParams()[4].getClass(), int[].class);

        System.out.println("Double[].class:" + stepStatus_1.getParams()[5].getClass());
        Assert.assertEquals(stepStatus_1.getParams()[5].getClass(), Double[].class);
    }

    @Test(expected = ConfigReadException.class)
    public void testResolveResponseParam() throws UnknownExceptionInRunning, ConfigReadException {
        Transfer transfer = workflowConfig.getSteps().get(0).getTransfer();
        paramsResolver.resolverTransferParams(transfer);

        StepStatus stepStatus_1 = workflowResolver.getWorkflowInstance().getStepStatusesMap().get("sign-1");

        WFResponse response_1 = new WFResponse();
        response_1.setCode(200);
        response_1.setMsg("OK");
        HashMap<String, String> body = new HashMap<>();
        body.put("test1", "test");
        body.put("test2", "1.23");
        response_1.setBody(body);
        stepStatus_1.setWfResponse(response_1);

        // step2的参数根据step1的输出结果确定
        transfer = workflowConfig.getSteps().get(1).getTransfer();
        Object[] params_2 = paramsResolver.resolverTransferParams(transfer);
        Assert.assertEquals(params_2[1].getClass(), Double.class);

        body.put("test2", "asdf");
        paramsResolver.resolverTransferParams(transfer);
    }

    @Test
    public void testScatterTo() {
        Step currentStep = workflowResolver.getCurrentStep();
        Assert.assertEquals(currentStep.getSign(), ConfigConstant.CONF_START_SIGN);

        Transfer transfer = workflowConfig.getSteps().get(0).getTransfer();
        List<Transfer> steps = workflowResolver.getScatterBranches(transfer);
        Assert.assertEquals(steps.get(1).getTo(), "sign-3");
    }

    static String wf = "{\n" +
            "  \"application\": \"application-test\",\n" +
            "  \"id\": \"wf-test\",\n" +
            "  \"startTime\":\"2016-01-01\",\n" +
            "  \"stopTime\":\"2017-12-01\",\n" +
            "\n" +

            "  \"steps\": [\n" +

            "    {\n" +
            "      \"sign\": \"&START\",\n" +
            "      \"name\": \"Start\",\n" +
            "      \"transfer\":{\n" +
            "        \"input\": [\n" +
            "          {\"name\": \"test1\",  \"type\": \"String\", \"value\": \"test\"},\n" +
            "          {\"name\": \"test2\",  \"type\": \"double\", \"value\": 11},\n" +
            "          {\"name\": \"test3\",  \"type\": \"int\", \"value\": 2},\n" +
            "          {\"name\": \"list\",  \"type\": \"List\", \"value\": \"[1,2]\"}," +
            "          {\"name\": \"list\",  \"type\": \"int[]\", \"value\": \"[3,4]\"}," +
            "          {\"name\": \"list\",  \"type\": \"Double[]\", \"value\": \"[5,6,7]\"}" +
            "        ],\n" +
            "\n" +
            "        \"to\": \"sign-1\",\n" +
            "        \"scatters\":[" +
            "                      {\"to\": \"sign-2\"}," +
            "                      {\"to\": \"sign-3\"}" +
            "               ]" +
            "      }\n" +
            "    },\n" +


            "    {\n" +
            "      \"sign\": \"sign-1\",\n" +
            "      \"name\": \"step-test-1\",\n" +
            "      \"call\": \"class:org.radrso.test.TestWorkflow\",\n" +
            "      \"method\": \"testStep1\",\n" +
            "      \"loop\": 1,\n" +
            "\n" +
            "      \"transfer\": {\n" +

            "        \"input\": [\n" +
            "          {\"name\": \"test1\",  \"type\": \"String\", \"value\": \"{output}[sign-1][test1]\"},\n" +
            "          {\"name\": \"test2\",  \"type\": \"double\", \"value\": \"{output}[sign-1][test2]\"}\n" +
            "        ],\n" +

            "        \"to\": \"sign-3\"  \n" +

            "      }\n" +
            "    },\n" +


            "    {\n" +
            "      \"sign\": \"sign-2\",\n" +
            "      \"name\": \"step-test-2\",\n" +
            "      \"call\": \"class:org.radrso.test.TestWorkflow\",\n" +
            "      \"method\": \"testStep2\",\n" +
            "      \"loop\": 1,\n" +
            "\n" +
            "      \"transfer\": {\n" +
            "\n" +
            "        \"judge\":\n" +
            "        {\n" +
            "          \"compute\": \"{output}[sign-2][test2]\",\n" +
            "          \"computeWith\": 1000,\n" +
            "          \"type\": \"double\",\n" +
            "          \"expression\": \"<\",\n" +
            "          \"passTransfer\":{\n" +
            "            \"input\":[],\n" +
            "            \"to\": \"sign-3\"\n" +
            "          },\n" +
            "          \"nopassTransfer\":{\n" +
            "            \"input\":[],\n" +
            "            \"to\": \"start\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +


            "    {\n" +
            "      \"sign\": \"sign-3\",\n" +
            "      \"name\": \"step-test-5\",\n" +
            "      \"call\": \"class:org.radrso.test.TestWorkflow\",\n" +
            "      \"method\": \"testStep5\",\n" +
            "      \"loop\": 1,\n" +
            "\n" +
            "      \"transfer\": {\n" +
            "\n" +
            "        \"deadline\":\"2017-11-12\",\n" +
            "        \"to\": \"&FINISH\"\n" +
            "      }\n" +
            "    },\n" +


            "    {\n" +
            "      \"sign\": \"&FINISH\",\n" +
            "      \"name\": \"Finish the workflow\",\n" +
            "      \"call\": \"class:org.radrso.test.TestWorkflow\",\n" +
            "      \"method\": \"finish\"\n" +
            "    }\n" +
            "  ]\n" +

            "}";
}
