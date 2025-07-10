# Axum Matching Engine

> **Ultra-fast order-matching core in Java**, now extended with:
> - **FIX gateway** (QuickFIX/J)  
> - **REST gateway** (Spring-Boot)  
> - **Docker & Docker-Compose**  

---

## 📖 Overview

`Axum Matching Engine` is a high-performance, low-latency order-matching engine based on:

- [LMAX Disruptor](https://github.com/LMAX-Exchange/disruptor)  
- [Eclipse Collections](https://www.eclipse.org/collections/)  
- [Real Logic Agrona](https://github.com/real-logic/agrona)  
- [OpenHFT Chronicle-Wire](https://github.com/OpenHFT/Chronicle-Wire)  
- [LZ4 Java](https://github.com/lz4/lz4-java)  
-  [Adaptive Radix Trees](https://db.in.tum.de/~leis/papers/ART.pdf)

Core features:

- Atomic, lock-free matching & risk control  
- Two risk modes **(direct-exchange / margin-trade)**  
- In-memory order books & account state  
- Event-sourcing: disk journaling **+ snapshots / restore**  
- Maker/taker fee engine & reporting API  
- Pluggable “Naive” vs “Direct” order-book implementations  
- Supports IOC, GTC, FOK-Budget orders  
- **Ultra-low GC pressure** (object pools, single ring buffer)  
- **Thread affinity** option via JNA for tail-latency tuning


_Single-symbol bench on Xeon X5690 (RHEL7)_: up to **5 M ops/sec**, sub-µs median.

| Rate  | 50%   | 90%   | 95%   | 99%   | 99.9% | 99.99% | Worst |
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:------:|:-----:|
| 125 K | 0.6 µs | 0.9 µs | 1.0 µs | 1.4 µs | 4 µs  | 24 µs  | 41 µs |
| 250 K | 0.6 µs | 0.9 µs | 1.0 µs | 1.4 µs | 9 µs  | 27 µs  | 41 µs |
| 500 K | 0.6 µs | 0.9 µs | 1.0 µs | 1.6 µs | 14 µs | 29 µs  | 42 µs |
| 1 M   | 0.5 µs | 0.9 µs | 1.2 µs | 4 µs   | 22 µs | 31 µs  | 45 µs |
| 2 M   | 0.5 µs | 1.2 µs | 3.9 µs | 10 µs  | 30 µs | 39 µs  | 60 µs |
| 3 M   | 0.7 µs | 3.6 µs | 6.2 µs | 15 µs  | 36 µs | 45 µs  | 60 µs |
| 4 M   | 1.0 µs | 6.0 µs | 9 µs   | 25 µs  | 45 µs | 55 µs  | 70 µs |
| 5 M   | 1.5 µs | 9.5 µs | 16 µs  | 42 µs  | 150 µs| 170 µs | 190 µs|
| 6 M   | 5 µs   | 30 µs  | 45 µs  | 300 µs | 500 µs| 520 µs | 540 µs|
| 7 M   | 60 µs  | 1.3 ms | 1.5 ms | 1.8 ms | 1.9 ms| 1.9 ms | 1.9 ms|

![Latencies HDR Histogram](hdr-histogram.png)

<details>
<summary><strong>Benchmark configuration (for the latency table above)</strong></summary>

- Single symbol order book  
- 3 M inbound msgs: 9 % GTC, 3 % IOC, 6 % cancel, 82 % move (≈ 6 % trade-triggering)  
- 1 000 active users, ~1 000 live orders in ~750 price slots  
- Measurements cover **risk + matching only** (no network / IPC)  
- Non-bursty feed (0.2–8 µs gaps) to avoid coordinated omission  
- GC forced before/after each 3 M-msg cycle  
- HW: dual Xeon X5690 @ 3.47 GHz (RHEL 7.5, tuned-adm *network-latency*)  
- JDK 8 u192 (later 8 u builds hit <https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8221355>)
</details>

---

## 🚀 What’s New

1. **FIX Gateway**  
   - QuickFIX/J acceptor in `FixEngine.java`  
   - Supports NewOrderSingle, Cancel/Replace, OrderStatusRequest, MarketDataRequest...  
   - Config in `src/main/resources/quickfix.cfg`

2. **REST Gateway**  
   - Spring-Boot endpoints:  
     - `POST /api/orders` → place/cancel/replace  
     - `GET  /api/orderbook/{sym}` → L2 data  
     - `GET  /api/users/{uid}/balance`  
   - Runs in `rest-api` container

3. **Docker & Compose**  
   - Multi-stage `Dockerfile` for engine + QFJ  
   - `docker-compose.yml` brings up:  
     - `axum-server` (core+FIX)  
     - `rest-api`    (REST)  
   - (*Optionally* `frontend`)

4. **Frontend Ready**  
   - The Next.js/Tailwind UI **is not included** here, plug in your own frontend 

---


## 📦 Quickstart (Backend-Only)

1. **Clone**  
   ```bash
   git clone https://github.com/YourUser/axum-matching-engine.git
   cd axum-matching-engine
   ```

2. **Launch**

   ```bash
   docker-compose up --build
   ```

   * FIX acceptor on **5001**
   * REST API on **8081**

3. **Smoke-Test FIX**

   ```bash
   printf "8=FIX.4.4|9=...|35=D|49=AXUM_ENGINE|56=TEST_COUNTERPARTY|…|10=128|\r\n" \
     | nc localhost 5001
   ```

4. **Smoke-Test REST**

   ```bash
   curl -X POST http://localhost:8081/api/orders \
     -H "Content-Type: application/json" \
     -d '{"uid":301,"symbol":1001,"price":150000,"size":1,"action":"BID","orderType":"GTC"}'
   ```

---

## ⚙️ Configuration

### FIX (`src/main/resources/quickfix.cfg`)

```ini
[default]
FileStorePath=store
FileLogPath=log
ConnectionType=acceptor
HeartBtInt=30
ReconnectInterval=60
SenderCompID=AXUM_ENGINE
TargetCompID=TEST_COUNTERPARTY

[session]
BeginString=FIX.4.4
StartTime=00:00:00
EndTime=23:59:59
SocketAcceptPort=5001
```

### REST (`rest-api/src/main/resources/application.yml`)

```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1
```

---

## 💻 Java API Example

```java
// Start engine
ExchangeCore core = ExchangeCore.builder()
    .resultsConsumer(new SimpleEventsProcessor(handler))
    .exchangeConfiguration(ExchangeConfiguration.defaultBuilder().build())
    .build();
core.startup();
ExchangeApi api = core.getApi();

// Define a symbol & add two users
api.submitBinaryDataAsync(new BatchAddSymbolsCommand(spec)).get();
api.submitCommandAsync(ApiAddUser.builder().uid(301L).build()).get();
api.submitCommandAsync(ApiAddUser.builder().uid(302L).build()).get();

// Deposit funds & place a GTC bid
api.submitCommandAsync(ApiAdjustUserBalance.builder()
    .uid(301L).currency(2).amount(1_000_000_000L).transactionId(1L).build()).get();
api.submitCommandAsync(ApiPlaceOrder.builder()
    .uid(301L).orderId(1L).symbol(1001).price(150_000L)
    .size(1L).action(OrderAction.BID).orderType(OrderType.GTC).build()).get();
```

---

## 🚧 Roadmap

* ✅ FIX & REST gateways
* ✅ Docker orchestration
* ✅ Internal regression test-suite
* 🔲 Market-data multicast feed (L1/L2/BBO)
* 🔲 Clearing & settlement API
* 🔲 Clustering & HA
* 🔲 Websocket / gRPC interfaces

---

## 🤝 Contributing

Contributions welcome! See \[CONTRIBUTING.md].

