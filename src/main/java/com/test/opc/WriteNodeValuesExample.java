package com.test.opc;

import com.google.common.collect.ImmutableList;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WriteNodeValuesExample implements ClientExample {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        client.connect().get();

        List<NodeId> nodeIds = ImmutableList.of(new NodeId(2, "2lsle421hc.AB_LV"));

        for (int i = 0; i < 10; i++) {
            Variant v = new Variant(i);

            DataValue dv = new DataValue(v, null, null);

            CompletableFuture<List<StatusCode>> completableFuture =
                    client.writeValues(nodeIds, ImmutableList.of(dv));

            List<StatusCode> statusCodes = completableFuture.get();
            StatusCode status = statusCodes.get(0);

            if (status.isGood()) {
                logger.info("Wrote '{}' to nodeId={}", v, nodeIds.get(0));
            }
        }

        NodeId nodeId = new NodeId(2, "2");
//        UaVariableNode node = new UaVariableNode();

        future.complete(client);
    }

    public static void main(String[] args) {
        new ClientExampleRunner(new WriteNodeValuesExample()).run();
    }
}
