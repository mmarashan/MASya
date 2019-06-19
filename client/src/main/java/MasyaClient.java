import model.MessageDTO;
import org.apache.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MasyaClient {

    private String username;
    private String password;

    private static Logger logger = Logger.getLogger(MasyaClient.class);

    StompSession connect(String username, String password) {

        this.username = username;
        this.password = password;

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        //sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

        StompHeaders stompHeaders = new StompHeaders();

        stompHeaders.set("username" ,username);
        stompHeaders.set("password" ,password);


        String url = "ws://{host}:{port}/ws";
        ListenableFuture<StompSession> f = stompClient.connect(url, headers, stompHeaders, new MyHandler(), "0.0.0.0", 8010);
        try {
            return f.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            logger.error("Bad connection. Check you credentials in headers!");
            return null;
        }
    }

    public void subscribeOnMessage(StompSession stompSession) throws ExecutionException, InterruptedException {
        stompSession.subscribe("/message.new/"+this.username, new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                logger.info("Received: " + new String(stompHeaders.toString()));
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                logger.info("Received: " + new String((byte[]) o));
            }
        });
    }

    public void send(StompSession stompSession, String payload) {
        MessageDTO dto = new MessageDTO();
        dto.setReceiver("t1");
        dto.setSender("t1");
        dto.setType(MessageDTO.MessageType.MESSAGE);
        dto.setTag("t1t1t1");
        dto.setTextContent(payload);
        StompHeaders stompHeaders = new StompHeaders();

        stompHeaders.set("username" ,"t1");
        stompHeaders.set("password" ,"t1");
        stompHeaders.setDestination("/message.send");
        // stompHeaders.setContentType(new MimeType("BYTES", "", Charset.forName("UTF-8")));

        stompSession.send(stompHeaders, dto.toString().getBytes(Charset.forName("UTF-8")));
    }

    private class MyHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            logger.info("Now connected");
        }
    }

    public static void main(String[] args) throws Exception {
        MasyaClient helloClient = new MasyaClient();

        StompSession stompSession = helloClient.connect("t1","t1");
        if (stompSession==null){
            return;
        }

        logger.info("Subscribing to greeting topic using session " + stompSession);
        helloClient.subscribeOnMessage(stompSession);

        logger.info("Sending hello message" + stompSession);
        helloClient.send(stompSession, "Hello me!");
        Thread.sleep(5000);
    }

}