package eu.lucaventuri.fibry.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import eu.lucaventuri.common.Exceptions;
import eu.lucaventuri.fibry.ActorSystem;
import eu.lucaventuri.fibry.distributed.HttpChannel;
import eu.lucaventuri.fibry.distributed.JacksonSerDeser;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class FibrySpringWebServerDemo {
    public static void main(String[] args) {
        SpringApplication.run(FibrySpringWebServerDemo.class, args);
    }

    @PostConstruct
    public void started() throws ExecutionException, InterruptedException {
        new Thread(() -> {
            ActorSystem.named("testActorGet").newActorWithReturn((String str) -> (str.toLowerCase() + "-GET"));
            ActorSystem.named("testActorPut").newActorWithReturn((String str) -> str.toLowerCase() + "-PUT");
            ActorSystem.named("testActorPost").newActorWithReturn((String str) -> str.toLowerCase() + "-POST");
            ActorSystem.named("testActorSync").newSynchronousActorWithReturn((String str) -> str.toLowerCase() + "-SYNC");

            Exceptions.silence(() -> {
                String url = "http://localhost:8080/fibry/messages";
                var channelGet = new HttpChannel(url, HttpChannel.HttpMethod.GET, null, null, false);
                var channelPut = new HttpChannel(url, HttpChannel.HttpMethod.PUT, null, null, false);
                var channelPost = new HttpChannel(url, HttpChannel.HttpMethod.POST, null, null, false);

                var actorGet = ActorSystem.anonymous().newRemoteActorWithReturn("testActorGet", channelGet, new JacksonSerDeser<>(String.class));
                var actorGet2 = ActorSystem.anonymous().newRemoteActor("testActorGet", channelGet, new JacksonSerDeser<>(String.class));
                var actorPut = ActorSystem.anonymous().newRemoteActorWithReturn("testActorPut", channelPut, new JacksonSerDeser<>(String.class));
                var actorPost = ActorSystem.anonymous().newRemoteActorWithReturn("testActorPost", channelPut, new JacksonSerDeser<>(String.class));
                var actorSync = ActorSystem.anonymous().newRemoteActorWithReturn("testActorSync", channelPut, new JacksonSerDeser<>(String.class));

                System.out.println("Waiting");
                Thread.sleep(1000);
                System.out.println("Sending messages");
                System.out.println("Received: " + actorGet.sendMessageReturn("testMessageGet").get());
                actorGet2.sendMessage("testMessageGet2");
                System.out.println("Received: " + actorPut.sendMessageReturn("testMessagePut").get());
                System.out.println("Received: " + actorPost.sendMessageReturn("testMessagePost").get());
                System.out.println("Received: " + actorSync.sendMessageReturn("testMessageSync").get());
            });
        }).start();
    }
}
