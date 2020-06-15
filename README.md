# Gorilla NREPL Middleware [![GitHub Actions status |pink-gorilla/gorilla-middleware](https://github.com/pink-gorilla/gorilla-middleware/workflows/CI/badge.svg)](https://github.com/pink-gorilla/gorilla-middleware/actions?workflow=CI)[![Codecov Project](https://codecov.io/gh/pink-gorilla/gorilla-middleware/branch/master/graph/badge.svg)](https://codecov.io/gh/pink-gorilla/gorilla-middleware)[![Clojars Project](https://img.shields.io/clojars/v/org.pinkgorilla/gorilla-middleware.svg)](https://clojars.org/org.pinkgorilla/gorilla-middleware)

- nrepl websocket relay (browsers can not connect to normal tcp ports)
- middleware that renders evalued results
- nrepl client 

 
## Demo

```
npm install
lein relay
lein demo

```

## Design of websocket relay

Server
- Browser can only do websocket and not normal sockets.
- Server Relay runs as ring request-handler, and returns new websocket handler
- the websocket handler creates a new nrel client connection, and then
  forwards requests in both ways.
- currently we create nrepl server to connect to.

Browser:
- The code is spit into several stateful components.
- layer1 is websocket connection. Provides two core async channels.
  Manages connection / reconnection.
- layer2: request router. Manages pending requests, returns for each nrepl-op
  a dedicated return channel, which is sent fragment by fragment. 
  The request channel will be closed after the last fragment is received. 
- layer3:    

Logging:
- Lots of logging with timbre at DEBUG level
- If you dot want lots of output, set loglevel to INFO


# Notes new release / notebook integration:

- make api similar to existing apis (nrepl / cojupyter / xxx)
- connect to remote nrepl without client middleware setup?

nrepl ws relay:
- relay nrepl client is stored in session, but we dont have session 
  middleware on that route. I think it is recreating the connections for
  each request.
- how are nrepl relay connections disgarded?  
- no conflict with existing notebook implementations?




