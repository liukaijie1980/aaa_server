package com.example.radiusapi.utils;

import com.google.gson.JsonSyntaxException;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class DockerService {


    private static final String CRLF = "\r\n";

     //下面的路径和docker 的配置有关。
    private static final String DOCKER_SOCKET_PATH = "/var/run/docker.sock";
    private static final String HTTP_GET_REQUEST = "GET /services/myradius_radius HTTP/1.0\r\n\r\n";
    private static final String HTTP_POST_REQUEST_TEMPLATE = "POST /services/myradius_radius/update?version=%d HTTP/1.0\r\nContent-Type: application/json\r\nContent-Length: %d\r\n\r\n%s";

    //本函数通过URL访问docker api
    public void restartDockerService(String URL) throws IOException {

        String serviceData = get(URL);
        log.info("Service Data: " + serviceData);  // Log the service data
        JsonObject jsonServiceData = JsonParser.parseString(serviceData).getAsJsonObject();


        int serviceVersion = jsonServiceData.getAsJsonObject("Version").get("Index").getAsInt();
        JsonObject serviceSpec = jsonServiceData.getAsJsonObject("Spec");

        int currentForceUpdate = serviceSpec.getAsJsonObject("TaskTemplate").get("ForceUpdate").getAsInt();
        int newForceUpdate = currentForceUpdate + 1;

        serviceSpec.getAsJsonObject("TaskTemplate").addProperty("ForceUpdate", newForceUpdate);

        post(URL+"/update?version=" + serviceVersion, serviceSpec.toString());

    }
    private String get(String urlString) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();

        return sb.toString();
    }

    private void post(String urlString, String payload) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to update service, HTTP error code: " + responseCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }
    //本函数通过unixSocket 访问docker api。需要将docker宿主机的/var/run/docker.sock，映射到到本进程所在容器。
    public void restartDockerService() throws IOException {
        log.info("restartDockerService");
        try {
            String serviceData = sendUnixSocketRequest(HTTP_GET_REQUEST);
            log.info("Service Data:\n" + serviceData);

            validateHttpResponse(serviceData);

            JsonObject jsonServiceData = parseResponseBody(serviceData);
            updateService(jsonServiceData);
        } catch (JsonSyntaxException e) {
            log.error("Malformed JSON response:", e);
            throw new IOException("Malformed JSON response", e);
        } catch (Exception e) {
            log.error("An error occurred:", e);
            throw new IOException("An error occurred while restarting the service", e);
        }
    }

    private void validateHttpResponse(String httpResponse) throws IOException {
        // Validate the HTTP response here
    }

    private JsonObject parseResponseBody(String serviceData) throws JsonSyntaxException {
        int bodyStartIndex = serviceData.indexOf("{");
        if (bodyStartIndex == -1) {
            throw new JsonSyntaxException("JSON body missing opening brace");
        }
        String body = serviceData.substring(bodyStartIndex);
        return JsonParser.parseString(body).getAsJsonObject();
    }

    private void updateService(JsonObject jsonServiceData) throws IOException {
        // Extract and validate JSON keys here

        int serviceVersion = jsonServiceData.getAsJsonObject("Version").get("Index").getAsInt();
        JsonObject serviceSpec = jsonServiceData.getAsJsonObject("Spec");

        int currentForceUpdate = serviceSpec.getAsJsonObject("TaskTemplate").get("ForceUpdate").getAsInt();
        int newForceUpdate = currentForceUpdate + 1;
        serviceSpec.getAsJsonObject("TaskTemplate").addProperty("ForceUpdate", newForceUpdate);

        String payload = serviceSpec.toString();
        String postRequest = String.format(HTTP_POST_REQUEST_TEMPLATE, serviceVersion, payload.length(), payload);
        log.info("postRequest:\n" + postRequest);
        sendUnixSocketRequest(postRequest);
    }

    private String sendUnixSocketRequest(String httpRequest) throws IOException {
        UnixSocketAddress address = new UnixSocketAddress(DOCKER_SOCKET_PATH);
        try (UnixSocketChannel channel = UnixSocketChannel.open(address)) {
            try (OutputStream out = Channels.newOutputStream(channel);
                 BufferedReader br = new BufferedReader(Channels.newReader(channel, "UTF-8"));
                 WritableByteChannel wbc = Channels.newChannel(out)) {
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                buffer.put(httpRequest.getBytes());
                buffer.flip();
                wbc.write(buffer);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        }
    }


}
