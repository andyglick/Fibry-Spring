package eu.lucaventuri.fibry.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.lucaventuri.fibry.ActorSystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletRequest;

// TODO: check Authentication
@RestController
public class FibryController {
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    FibryProperties properties;
    private static final Logger logger= LoggerFactory.getLogger(FibryController.class);

    @CrossOrigin
    @GetMapping("/fibry/messages")
    String handleFibryMessageGet(ServletRequest request,
      String actorName, String type, String message, boolean waitResult)
      throws ClassNotFoundException, JsonProcessingException, ExecutionException, InterruptedException, UnknownHostException {
        return processActorMessage(request, actorName, type, message, waitResult);
    }

    private String processActorMessage(ServletRequest request,
      String actorName, String type, String message, boolean waitResult)
      throws UnknownHostException, JsonProcessingException, ClassNotFoundException,
      InterruptedException, ExecutionException {

        checkPermissions(actorName, request);
        var obj = mapper.readValue(message, Class.forName(type));

        if (properties.isDebug())
            logger.info("Actor {} received {} message of type {}: {} ", actorName, waitResult ? "a synchronous" : "an asynchronous", type, message);

        if (waitResult)
            return mapper.writeValueAsString(ActorSystem.sendMessageReturn(actorName, obj, properties.isForceDelivery()).get());
        else {
            ActorSystem.sendMessage(actorName, obj, properties.isForceDelivery());

            return "{\"status\": \"SENT\"}";
        }
    }

    @CrossOrigin
    @PutMapping("/fibry/messages")
    String handleFibryMessagePut(ServletRequest request,
      String actorName, String type, @RequestBody String message,
      boolean waitResult) throws ClassNotFoundException, JsonProcessingException,
      ExecutionException, InterruptedException, UnknownHostException {
        return processActorMessage(request, actorName, type, message, waitResult);
    }

    @CrossOrigin
    @PostMapping("/fibry/messages")
    String handleFibryMessagePost(ServletRequest request,
      String actorName, String type, @RequestBody String message,
      boolean waitResult) throws ClassNotFoundException, JsonProcessingException,
      ExecutionException, InterruptedException, UnknownHostException {
        return processActorMessage(request, actorName, type, message, waitResult);
    }

    private void checkPermissions(String actorName, ServletRequest request) throws UnknownHostException {
        if (actorName==null)
            throw new IllegalArgumentException("The actor name cannot be null");

        if (properties.isPrivateIpOnly()) {
            InetAddress addr = InetAddress.getByName(request.getRemoteAddr());

            if (!addr.isLoopbackAddress() && !addr.isSiteLocalAddress())
                throw new SecurityException("Actors HTTP calls not allowed from " + addr.toString());
        }

        for(var name: properties.getExposedActors()) {
            if (actorName.equals(name))
                return; // allowed
        }

        throw new SecurityException("No actor named " + actorName + " has been exposed to HTTP");
    }
}
