/*
 * Copyright (c) 2021 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.test.opc;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

import java.util.concurrent.CompletableFuture;


public interface ClientExample {
    void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception;
}
