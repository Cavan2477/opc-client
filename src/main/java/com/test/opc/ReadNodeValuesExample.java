package com.test.opc;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReadNodeValuesExample implements ClientExample {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        client.connect().get();
        List<NodeId> nodeIds = new ArrayList<NodeId>() {{
            add(new NodeId(2, "2lsle421hc.AB_LV"));
            add(new NodeId(2, "2lsle421hc.BC_LV"));
            add(new NodeId(2, "2lsle421hc.CA_lv"));
            add(new NodeId(2, "2lsle421hc.Cos"));
            add(new NodeId(3, "Code1"));
            add(new NodeId(0, 40));
            add(new NodeId(3, 1008));
            add(new NodeId(0, 63));
            add(new NodeId(4, 2));
            add(new NodeId(4, 3));
            add(new NodeId(4, 4));
            add(new NodeId(4, 5));
            add(new NodeId(4, 6));
            add(new NodeId(4, 7));
            add(new NodeId(4, 8));
        }};
        CompletableFuture<List<DataValue>> f = client.readValues(1, TimestampsToReturn.Server, nodeIds);
        List<DataValue> dataValueList = f.get();

        for (DataValue dataValue : dataValueList) {
            logger.info("read value={}", dataValue.getValue());
        }

        future.complete(client);
    }

    public static void main(String[] args) {
        new ClientExampleRunner(new ReadNodeValuesExample()).run();
    }
}
