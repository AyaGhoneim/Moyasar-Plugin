/*
 * Copyright 2021 Wovenware, Inc
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.moyasar.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.killbill.billing.plugin.moyasar.core.MoyasarConfigProperties;
import org.killbill.billing.plugin.moyasar.core.MoyasarConfigPropertiesConfigurationHandler;
import org.killbill.billing.util.callcontext.TenantContext;

public class MoyasarClientImpl implements MoyasarClient {


    private String publicKey = "pk_test_sJJkeDCbkfpNfZoLt3kqnGhQAobuzV8jKZqBDH5Z";
    private String apiKey = "sk_test_FDAuTDgZKygmEmoSLjkwAXHRj6jwYFW992BkBRe5";

    @Override
    public String createPaymentMethod(String name , String Card_Number , String month , String year , String cvc) {

        // API endpoint
        String url = "https://api.moyasar.com/v1/tokens";
        // Data payload
        String formData = buildFormData(
                "name", name,
                "number", Card_Number,
                "month", month,
                "year", year,
                "cvc", cvc,
                "callback_url", "https://mystore.com/thanks"
        );

        try {
            // Encode API key for Basic Auth
            String auth = Base64.getEncoder().encodeToString((publicKey + ":").getBytes());

            // Create HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Create HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            // Send request and get response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Output response
            System.out.println("Response Code: " + response.statusCode());
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("id"); // Assuming "id" contains the token value
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map getPaymentMethod(String token) throws IOException, InterruptedException {

        // API endpoint to retrieve token details
        String endpoint = "https://api.moyasar.com/v1/tokens/" + token;

        // Encode API key for Basic Auth
        String auth = Base64.getEncoder().encodeToString((apiKey + ":").getBytes());

        // Create HTTP Client
        HttpClient client = HttpClient.newHttpClient();

        // Build HTTP Request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Basic " + auth)
                .GET()
                .build();

        // Send HTTP Request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response status
        if (response.statusCode() != 200) {
            try {
                throw new Exception("Failed to retrieve token details. Response Code: " + response.statusCode() + ", Response: " + response.body());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        String responseBody = response.body();
        ObjectMapper objectMapper = new ObjectMapper();

            // Convert the response body into a Map<String, Object>
        try {
            return objectMapper.readValue(responseBody, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    // Helper method to build form-encoded data
    private static String buildFormData(String... keyValues) {
        StringBuilder formData = new StringBuilder();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) formData.append("&");
            formData.append(URLEncoder.encode(keyValues[i], StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(keyValues[i + 1], StandardCharsets.UTF_8));
        }
        return formData.toString();
    }
}



