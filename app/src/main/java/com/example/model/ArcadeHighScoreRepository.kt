package com.example.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "arcade_high_scores")

data class ArcadeStats(
  val highScore: Int = 0,
  val bossesDefeated: Int = 0,
  val collectablesFound: Int = 0,
  val arcadeRank: String = "Novice Voyager",
  val latestLoreEntry: String = "Codex #01: The Astral Rift awakens beneath the celestial lavender sun."
)

class ArcadeHighScoreRepository(private val context: Context) {

  private object Keys {
    val HIGH_SCORE = intPreferencesKey("high_score")
    val BOSSES_DEFEATED = intPreferencesKey("bosses_defeated")
    val COLLECTABLES_FOUND = intPreferencesKey("collectables_found")
    val ARCADE_RANK = stringPreferencesKey("arcade_rank")
    val LATEST_LORE = stringPreferencesKey("latest_lore")
  }

  val arcadeStatsFlow: Flow<ArcadeStats> = context.dataStore.data.map { preferences ->
    val score = preferences[Keys.HIGH_SCORE] ?: 12450
    val bosses = preferences[Keys.BOSSES_DEFEATED] ?: 3
    val collectables = preferences[Keys.COLLECTABLES_FOUND] ?: 18
    val rank = preferences[Keys.ARCADE_RANK] ?: "S-Rank Celestial Voyager"
    val lore = preferences[Keys.LATEST_LORE] ?: "Codex #04: Titan of the Astral Rift fractured into shimmering stardust."

    ArcadeStats(
      highScore = score,
      bossesDefeated = bosses,
      collectablesFound = collectables,
      arcadeRank = rank,
      latestLoreEntry = lore
    )
  }

  suspend fun recordArcadeScore(
    score: Int,
    bosses: Int,
    collectables: Int,
    rank: String,
    lore: String
  ) {
    context.dataStore.edit { preferences ->
      val currentHigh = preferences[Keys.HIGH_SCORE] ?: 0
      if (score > currentHigh) {
        preferences[Keys.HIGH_SCORE] = score
      }
      if (bosses > (preferences[Keys.BOSSES_DEFEATED] ?: 0)) {
        preferences[Keys.BOSSES_DEFEATED] = bosses
      }
      if (collectables > (preferences[Keys.COLLECTABLES_FOUND] ?: 0)) {
        preferences[Keys.COLLECTABLES_FOUND] = collectables
      }
      if (rank.isNotEmpty()) {
        preferences[Keys.ARCADE_RANK] = rank
      }
      if (lore.isNotEmpty()) {
        preferences[Keys.LATEST_LORE] = lore
      }
    }
  }

  suspend fun resetScores() {
    context.dataStore.edit { preferences ->
      preferences[Keys.HIGH_SCORE] = 0
      preferences[Keys.BOSSES_DEFEATED] = 0
      preferences[Keys.COLLECTABLES_FOUND] = 0
      preferences[Keys.ARCADE_RANK] = "Novice Voyager"
      preferences[Keys.LATEST_LORE] = "Codex #01: The Astral Rift awaits your exploration."
    }
  }
}
