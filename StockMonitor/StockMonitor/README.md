# Stock Monitor — Android Portfolio & Watchlist Tracker

A real-time stock monitoring app built with Kotlin and Jetpack Compose. Users can search stocks, build a watchlist, track a personal portfolio with live gain/loss calculation, and set price alerts backed by a background job.

## Features
- **Search** any stock symbol/company (Alpha Vantage `SYMBOL_SEARCH`)
- **Watchlist** with live price + % change, color-coded gain/loss
- **Stock detail** screen with a 30-day closing-price line chart
- **Portfolio tracker** — enter quantity + buy price, see live gain/loss
- **Price alerts** — set a target price; a `WorkManager` job checks every 15 minutes and fires a local notification when the target is crossed
- **Offline support** — Room caches the last-fetched price so the watchlist still renders without network, with a "last updated" timestamp

## Tech Stack
| Layer          | Choice                                   |
|----------------|-------------------------------------------|
| UI             | Jetpack Compose + Material 3              |
| Architecture   | MVVM + Repository pattern                 |
| Local storage  | Room                                      |
| Networking     | Retrofit + Gson                           |
| DI             | Hilt                                      |
| Background work| WorkManager                               |
| Async          | Kotlin Coroutines + Flow                  |
| API            | [Alpha Vantage](https://www.alphavantage.co/) (free tier) |

## Architecture

```
UI (Compose screens)
      ↓
ViewModel (StockViewModel)
      ↓
Repository (StockRepository)
      ↓                    ↓
Room (local cache)   Retrofit (remote API)
```

The repository is the single source of truth: it always writes fresh network data into Room, and the UI observes Room via `Flow`, so screens update automatically and stay populated even offline.

## Setup

1. Clone the repo and open in Android Studio (Koala or newer recommended).
2. Get a free API key from [Alpha Vantage](https://www.alphavantage.co/support/#api-key).
3. In `app/build.gradle.kts`, replace:
   ```kotlin
   buildConfigField("String", "STOCK_API_KEY", "\"YOUR_ALPHA_VANTAGE_API_KEY\"")
   ```
   with your key.
4. Sync Gradle and run on an emulator or device (min SDK 24).

> Note: Alpha Vantage's free tier allows 5 requests/minute and 100/day. If you hit the limit while testing, wait a minute or switch the `StockApiService` base URL / DTOs to another provider such as [Finnhub](https://finnhub.io/).

## Project Structure
```
app/src/main/java/com/karthi/stockmonitor/
├── data/
│   ├── local/        # Room entities, DAO, database
│   ├── remote/        # Retrofit service + DTOs
│   └── repository/    # StockRepository — single source of truth
├── di/                # Hilt modules
├── ui/screens/         # Compose screens (Watchlist, Search, Detail, Portfolio)
├── viewmodel/         # StockViewModel
├── worker/            # PriceAlertWorker (WorkManager)
├── MainActivity.kt
└── StockMonitorApp.kt
```

## Possible Extensions
- Swap the hand-rolled `LineChart` composable for MPAndroidChart if richer charting is needed
- Add a home-screen widget showing the top watchlist stock
- Add a simple moving-average trend indicator

## Background
This project evolved from an earlier Java/XML stock-search app built during my BCA coursework, rebuilt here with a modern Kotlin/Compose stack, live data, offline caching, and background alerting.
