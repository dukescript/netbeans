/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.autoupdate.jdk11;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.time.Duration;
import java.net.http.*;
import static java.net.http.HttpClient.*;
import java.nio.file.Paths;
import org.netbeans.spi.autoupdate.UseHttpSpi;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = UseHttpSpi.class)
public class UseHttpClient implements UseHttpSpi {
    public void demo() throws Exception {
        HttpClient client = HttpClient.newBuilder()
             .version(Version.HTTP_1_1)
             .followRedirects(Redirect.NORMAL)
             .connectTimeout(Duration.ofSeconds(20))
             .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
             .authenticator(Authenticator.getDefault())
             .build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://foo.com/"))
            .timeout(Duration.ofMinutes(2))
            .header("Content-Type", "application/json")
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());  

    }
}
