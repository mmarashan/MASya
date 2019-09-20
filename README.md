## Message Asynch Server on Java

Lightweight Message Asynch Server written on Java (using Spring-Boot Websocket) for creating real-time data transmittion projects: chats, web-apps, IoT.

Opportunities:
- Auth by username/password
- Hold message for offline members
- Protocol using submiting input messages for sender

![](masya_chat_demo.gif)

Run server directly

```bash
cd server/
mvn spring-boot:run
```

Run using docker-compose:

```bash
docker-compose up
```

TODO:
- Api for check is online member
- Members management page
- Bynary data transmition
- Any language clients (Java, Python, JS...)
- Channels and subscriptions

Issues:
- non send message when start via compose with nginx
- bad markup for Disconnect button
- too short roomCode based on sessionId