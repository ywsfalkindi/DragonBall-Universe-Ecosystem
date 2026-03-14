package com.saiyan.dragonballuniverse.quiz

import android.util.Log
import com.saiyan.dragonballuniverse.network.PocketBaseClient
import com.saiyan.dragonballuniverse.network.PocketBaseUpsertUserStatsBody
import com.saiyan.dragonballuniverse.network.PocketBaseUserStatsRecord

/**
 * Quiz data source that encapsulates direct PocketBase access.
 *
 * Important: This repository preserves existing behavior and request parameters from QuizViewModel.
 */
class QuizRepository {

    suspend fun listQuizQuestions(
        page: Int = 1,
        perPage: Int = 200,
    ) = PocketBaseClient.apiService.listQuizQuestions(page = page, perPage = perPage)

    /**
     * Sync stats from PocketBase (public user_stats collection).
     * If record doesn't exist yet, caller keeps local defaults and will create it on first update.
     */
    suspend fun syncStatsFromPocketBase(deviceId: String): PocketBaseUserStatsRecord? {
        val filter = """device_id="${deviceId}""""

        val resp =
            PocketBaseClient.apiService.listUserStats(
                filter = filter,
                page = 1,
                perPage = 1,
            )

        return resp.items.firstOrNull()
    }

    /**
     * Push stats to PocketBase (create if missing, otherwise patch).
     *
     * Collection is public (no auth) and keyed by device_id.
     */
    suspend fun pushStatsToPocketBase(
        deviceId: String,
        powerLevel: Long,
        senzuBeans: Int,
        highestStreak: Int,
        lastPlayedTimestamp: Long,
    ) {
        val filter = """device_id="${deviceId}""""

        val existing =
            PocketBaseClient.apiService
                .listUserStats(
                    filter = filter,
                    page = 1,
                    perPage = 1,
                )
                .items
                .firstOrNull()

        val body =
            PocketBaseUpsertUserStatsBody(
                deviceId = deviceId,
                powerLevel = powerLevel,
                senzuBeans = senzuBeans,
                highestStreak = highestStreak,
                lastPlayedTimestamp = lastPlayedTimestamp,
            )

        if (existing == null) {
            PocketBaseClient.apiService.createUserStats(body)
            Log.d("PB_DEBUG", "Quiz: created user_stats for device_id=$deviceId")
        } else {
            PocketBaseClient.apiService.updateUserStats(existing.id, body)
            Log.d("PB_DEBUG", "Quiz: updated user_stats id=${existing.id} for device_id=$deviceId")
        }
    }
}
