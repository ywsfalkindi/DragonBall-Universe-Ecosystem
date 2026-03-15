package com.saiyan.dragonballuniverse.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.saiyan.dragonballuniverse.db.migrations.MIGRATION_2_3

@Database(
    entities = [
        UserEpisodeEntity::class,
        UserStatsEntity::class,
        UserMangaProgressEntity::class,
        UserMangaDownloadEntity::class,
        UserMangaPageCacheEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun episodeDao(): EpisodeDao
    abstract fun userStatsDao(): UserStatsDao

    abstract fun mangaProgressDao(): MangaProgressDao
    abstract fun mangaDownloadDao(): MangaDownloadDao
    abstract fun mangaPageCacheDao(): MangaPageCacheDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase {
            Log.d("APP_DEBUG", "Checkpoint 6: UserDatabase getInstance called")
            return INSTANCE ?: synchronized(this) {
                Log.d("APP_DEBUG", "Checkpoint 7: Building/Opening database")
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_db",
                )
                    .addMigrations(MIGRATION_2_3)
                    // Safety for development builds: if a user comes from an older schema (e.g. v1)
                    // and we don't have a full migration chain, don't crash the app.
                    // NOTE: This will wipe local data; keep this scoped to DEBUG only.
                    .apply {
                        @Suppress("KotlinConstantConditions")
                        if (com.saiyan.dragonballuniverse.BuildConfig.DEBUG) {
                            fallbackToDestructiveMigration()
                        }
                    }
                    .build()
                    .also {
                        Log.d("APP_DEBUG", "Checkpoint 8: Database built/opened successfully")
                        INSTANCE = it
                    }
            }
        }
    }
}
