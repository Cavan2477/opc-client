package com.test.opc;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class ClientExampleRunner {
    private static final String endpoint = "opc.tcp://10.1.2.10:4862";
    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();
    private final Logger logger = LoggerFactory.getLogger(ClientExampleRunner.class);
    private ClientExample clientExample;

    public ClientExampleRunner(ClientExample clientExample) {
        this.clientExample = clientExample;
    }

    private OpcUaClient createClient() throws Exception {
        Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "client", "security");
        Files.createDirectories(securityTempDir);

        if (!Files.exists(securityTempDir)) {
            throw new Exception("unable to create security dir: " + securityTempDir);
        }

        File pkiDir = securityTempDir.resolve("pki").toFile();

        logger.info("security dir: {}", securityTempDir.toAbsolutePath());
        logger.info("security pki dir: {}", pkiDir.getAbsolutePath());

        KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

        return OpcUaClient.create(
                endpoint,
                endpoints -> endpoints.stream().filter(e -> SecurityPolicy.Basic256.getUri().equals(e.getSecurityPolicyUri())).findFirst(),
                config ->
                        config.setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
                                .setApplicationUri("urn:mt:milo:test:client")
                                .setKeyPair(loader.getClientKeyPair())
                                .setCertificate(loader.getClientCertificate())
                                .setCertificateChain(loader.getClientCertificateChain())
                                .setIdentityProvider(new AnonymousProvider())
                                .setRequestTimeout(uint(5000))
                                .build()
        );
    }

    private OpcUaClient createClient1() throws Exception {
        return OpcUaClient.create(endpoint);
    }

    private OpcUaClientConfig buildConfiguration(final List<EndpointDescription> endpoints) {
        final OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(findBest(endpoints));
        return cfg.build();

    }

    public EndpointDescription findBest(final List<EndpointDescription> endpoints) {
        return endpoints.stream().filter(e -> SecurityPolicy.Basic256.getUri().equals(e.getSecurityPolicyUri())).findFirst().get();
    }

    public void run() {
        try {
            OpcUaClient client = createClient1();
            future.whenCompleteAsync((c, ex) -> {
                if (ex != null) {
                    logger.error("Error running test: {}", ex.getMessage(), ex);
                }
                try {
                    client.disconnect().get();
                    Stack.releaseSharedResources();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error disconnecting: {}", e.getMessage(), e);
                }
                try {
                    Thread.sleep(1000);
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            try {
                this.clientExample.run(client, future);
                future.get(15, TimeUnit.SECONDS);
            } catch (Throwable t) {
                logger.error("Error running client example: {}", t.getMessage(), t);
                future.completeExceptionally(t);
            }

        } catch (Throwable t) {
            logger.error("Error getting client: {}", t.getMessage(), t);
            future.completeExceptionally(t);
            try {
                Thread.sleep(1000);
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(999_999_999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
