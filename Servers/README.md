# Masked-Intelligence

Websocket server

## Launch commands

First, compile to JIL with

```bash
mvn install
```

From this directory:

**HTTP server**

```bash
mvn exec:java -Dexec.mainClass="webServer.ServerREST" -Dexec.classpathScope=runtime
```

**WebSocket server**

```bash
mvn exec:java -Dexec.mainClass="websocketServer.WebSocketServer" -Dexec.classpathScope=runtime
```
