package com.aliev.job_task;

import jdk.swing.interop.SwingInterOpUtils;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@RestController
@RequestMapping("/api/")
public class VKBotController {
    @Autowired
    private Environment env;


    @PostMapping
    public Object handleRequest(@RequestBody Map<String, Object>requestBody) {
        var accessToken = env.getProperty("accessToken");

        ResponseEntity<Object> response;
        if (requestBody.get("type").equals("message_new")) {
            var object = (Map<String, Object>) requestBody.get("object");
            var message = (Map<String, Object>)object.get("message");
            var messageBody = (String)message.get("text");
            var authorId = (Integer) message.get("from_id");

            var apiResponse = sendMessageToVK(messageBody, authorId, accessToken);

            var apiResponseStatusCode = apiResponse.getStatusCode();
            if(!apiResponseStatusCode.is2xxSuccessful()) {
                response = new ResponseEntity<>(apiResponse.getBody(), apiResponseStatusCode);
            } else {
                response = new ResponseEntity<>("ok", HttpStatusCode.valueOf(200));
            }
        } else {
            response = new ResponseEntity<>("Unsupported action.", HttpStatusCode.valueOf(400));
        }
        return response;
    }

    private ResponseEntity<String> sendMessageToVK(String message, Integer peerId, String accessToken) {
        var answer = "Вы сказали: " + message;

        var restTemplate = new RestTemplate();
        var response = restTemplate.exchange(
                "https://api.vk.com/method/messages.send?" +
                        "&message=" + answer +
                        "&peer_id=" + peerId +
                        "&access_token=" + accessToken +
                        "&v=" + "5.199" +
                        "&random_id=" + "42",
                HttpMethod.GET,
                null,
                String.class
        );

        return response;
    }

}
