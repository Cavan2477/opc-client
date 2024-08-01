package com.test.opc;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ReadNodeValueExample implements ClientExample {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        client.connect().get();

//        NodeId nodeId = new NodeId(2, "2lsle421hc.AB_LV");

        NodeId nodeId = new NodeId(1, "t|float1");

//        CompletableFuture<DataValue> f = client.readValue(1, TimestampsToReturn.Both, nodeId);
//        DataValue v = f.get();
//        StatusCode statusCode = v.getStatusCode();
//        if (statusCode.isGood()) {
//            logger.info("read nodeId={} value={} ", nodeId.getIdentifier(), v.getValue());
//        }
//        logger.info("read nodeId={} value={} ", nodeId.getIdentifier(), v.getValue());
        UaVariableNode node = client.getAddressSpace().getVariableNode(nodeId);
        DataValue value = node.readValue();
//        future.complete(client);
        logger.info("Value={}", value);

        Variant variant = value.getValue();
        logger.info("Variant={}", variant.getValue());

        logger.info("BackingClass={}", BuiltinDataType.getBackingClass(variant.getDataType().get()));
    }

    public static void main(String[] args) {
        new ClientExampleRunner(new ReadNodeValueExample()).run();
    }
}
