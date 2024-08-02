package com.test.opc;

import com.google.common.collect.ImmutableList;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author CodeCaptain
 * @version 0.1.0
 * @description 订阅节点值样例
 * @date 2023/7/04 13:54
 */
public class SubscribeNodeValueExample implements ClientExample {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private OpcUaClient opcUaClient = null;
    private List<UaMonitoredItem> uaMonitoredItemList = null;

    @Override
    public void run(OpcUaClient opcUaClient, CompletableFuture<OpcUaClient> future) throws Exception {
        opcUaClient.connect().get();

        listNode(opcUaClient, null);

        // 查询订阅对象，没有则创建
        UaSubscription subscription = null;
        ImmutableList<UaSubscription> subscriptionList = opcUaClient.getSubscriptionManager().getSubscriptions();

        if (CollectionUtils.isEmpty(subscriptionList)) {
            subscription = opcUaClient.getSubscriptionManager().createSubscription(1000.0).get();
        } else {
            subscription = subscriptionList.get(0);
        }

        // 监控项请求列表
        List<MonitoredItemCreateRequest> requests = new ArrayList<>();

        // 创建监控的参数
        MonitoringParameters parameters = new MonitoringParameters(subscription.nextClientHandle(),
                1000.0, // sampling
                // interval
                null, // filter, null means use default
                Unsigned.uint(10), // queue size
                true // discard oldest
        );

        // 创建订阅的变量，创建监控项请求
        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                new ReadValueId(new NodeId(4, 4), AttributeId.Value.uid(),
                        null, null),
                MonitoringMode.Reporting, parameters);

        requests.add(request);

        // 创建监控项，并且注册变量值改变时候的回调函数
        uaMonitoredItemList = subscription.createMonitoredItems(TimestampsToReturn.Both, requests, (item, id) -> {
            item.setValueConsumer((i, v) -> {
                logger.info("item={}, value={}", i.getReadValueId().getNodeId(), v.getValue());
//                webSocket.GroupSending("item=" + i.getReadValueId().getNodeId() + ", value=" + v.getValue());
            });
        }).get();

        // 保持主线程运行，以便持续接收事件
        Thread.sleep(Long.MAX_VALUE);
    }

    public void listNode(OpcUaClient client, UaNode uaNode) throws Exception {
        List<? extends UaNode> uanodeList;

        if (uaNode == null) {
            uanodeList = client.getAddressSpace().browseNodes(Identifiers.ObjectsFolder);
        } else {
            uanodeList = client.getAddressSpace().browseNodes(uaNode);
        }

        for (UaNode uaNode_temp : uanodeList) {
            // 排除系统性节点，系统性节点名称一般以"_"开头
            if (!Objects.requireNonNull(uaNode_temp.getBrowseName().getName()).contains("_")) {
                continue;
            }

            System.out.println("Node= " + uaNode_temp.getBrowseName().getName() + ", namespace= " + uaNode_temp.getNodeId().getNamespaceIndex() + ", identifier= " + uaNode_temp.getNodeId().getIdentifier());

            // 递归调用以遍历子节点
            listNode(client, uaNode_temp);
        }
    }

    public static void main(String[] args) {
        new ClientExampleRunner(new SubscribeNodeValueExample()).run();
    }
}
