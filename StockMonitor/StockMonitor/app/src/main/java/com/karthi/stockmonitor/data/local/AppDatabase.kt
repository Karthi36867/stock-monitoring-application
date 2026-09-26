package com.karthi.stockmonitor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StockEntity::class, PortfolioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
}
