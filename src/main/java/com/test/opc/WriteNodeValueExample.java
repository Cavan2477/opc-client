package com.test.opc;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class WriteNodeValueExample implements ClientExample {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        client.connect().get();

//        NodeId nodeId = new NodeId(2, "2lsle421hc.AB_LV");
//        DataValue dv = new DataValue(new Variant(100), null, null);

        NodeId nodeId = new NodeId(4, 4);
        DataValue dataValue = new DataValue(new Variant("27"), null, null);
        UaVariableNode uaVariableNode = client.getAddressSpace().getVariableNode(nodeId);

        CompletableFuture<StatusCode> completableFuture = client.writeValue(nodeId, dataValue);
        StatusCode statusCode = completableFuture.get();

        logger.info("write nodeId={} value={} status={} ", nodeId.getIdentifier(), dataValue.getValue(), statusCode.isGood());

        future.complete(client);
    }

    public static void main(String[] args) {
        new ClientExampleRunner(new WriteNodeValueExample()).run();
    }
}
