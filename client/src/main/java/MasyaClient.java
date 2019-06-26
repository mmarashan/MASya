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

    private String roomCode;
    private StompSession session;

    private static Logger logger = Logger.getLogger(MasyaClient.class);

    boolean connect(String username, String password) {

        this.username = username;
        this.password = password;

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

        StompHeaders stompHeaders = new StompHeaders();

        stompHeaders.set("username" ,username);
        stompHeaders.set("password" ,password);

        String url = "ws://{host}:{port}/ws";
        ListenableFuture<StompSession> f = stompClient.connect(url, headers, stompHeaders, new MyHandler(), "0.0.0.0", 8010);
        try {
            session =  f.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            logger.error("Bad connection. Check you credentials in headers!");
            return false;
        }

        // subscribe on JOIN message and JOIN after
        session.subscribe("/message.new/"+this.username, new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                MessageDTO m = MessageDTO.fromJson(new String((byte[]) o));
                logger.info("Received and serialized: " + m.toJson()+"; "+stompHeaders.toString());
                if ("JOIN".equals(m.getType().name())){
                    roomCode = m.getTextContent();
                    subscribeOnMessage(session, roomCode);
                }
            }
        });

        return true;
    }


    private void subscribeOnMessage(StompSession stompSession, String roomCode) {
        stompSession.subscribe("/message.new/"+roomCode, new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                MessageDTO m = MessageDTO.fromJson(new String((byte[]) o));
                logger.info("Received and serialized: " + m.toJson()+"; "+stompHeaders.toString());
            }
        });
    }

    void send(String receiverCode, String payload) {
        MessageDTO dto = new MessageDTO();
        dto.setReceiver(receiverCode);
        dto.setSender(this.username);
        dto.setType(MessageDTO.MessageType.MESSAGE);
        dto.setTag("t1t1t1");
        dto.setTextContent(payload);
        StompHeaders stompHeaders = new StompHeaders();

        stompHeaders.set("username" ,username);
        stompHeaders.set("password" ,password);
        stompHeaders.setDestination("/message.send");


        session.send(stompHeaders, dto.toString().getBytes(Charset.forName("UTF-8")));
    }

    private class MyHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            logger.info("Now connected");
        }
    }

    public static void main(String[] args) throws Exception {
        MasyaClient helloClient = new MasyaClient();

        boolean connected = helloClient.connect("t1","t1");
        if (!connected){
            return;
        } else {
            // TODO: убрать необходимость этого засыпания. сервер не синхронизирован
            Thread.sleep(1000);
        }


        logger.info("Sending hello message");
        helloClient.send("t1", "Hello me!");
        Thread.sleep(5000);
    }

}