package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.audio.SoundManager
import com.example.game.CrawlerEngine
import com.example.model.ControlType
import com.example.model.PlayerCustomization
import com.example.multiplayer.MultiplayerManager
import com.example.ui.*
import com.example.ui.theme.DeepStarryNight
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
  TITLE,
  MULTIPLAYER_LOBBY,
  CHARACTER_SELECTION,
  GAME
}

class MainActivity : ComponentActivity() {

  private lateinit var soundManager: SoundManager
  private lateinit var multiplayerManager: MultiplayerManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    soundManager = SoundManager(this)
    multiplayerManager = MultiplayerManager(this)

    val prefs = getSharedPreferences("creepy_crawlers_prefs", Context.MODE_PRIVATE)
    val savedHighScore = prefs.getInt("high_score", 5600)
    val savedPlayerName = prefs.getString("player_name", "Crawler #1") ?: "Crawler #1"
    val savedColorIndex = prefs.getInt("player_color_idx", 0)
    val savedAntennaIndex = prefs.getInt("player_antenna_idx", 0)
    val savedEyeIndex = prefs.getInt("player_eye_idx", 0)

    soundManager.isSoundEnabled = prefs.getBoolean("sound_enabled", true)
    soundManager.isVibrationEnabled = prefs.getBoolean("vibration_enabled", true)

    setContent {
      MyApplicationTheme {
        var currentScreen by remember { mutableStateOf(Screen.TITLE) }
        var highScore by remember { mutableIntStateOf(savedHighScore) }
        var controlType by remember { mutableStateOf(ControlType.DPAD) }
        var isMultiplayerMode by remember { mutableStateOf(false) }

        var customization by remember {
          mutableStateOf(
            PlayerCustomization(
              name = savedPlayerName,
              colorIndex = savedColorIndex,
              antennaIndex = savedAntennaIndex,
              eyeIndex = savedEyeIndex
            )
          )
        }

        var showHowToPlay by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }

        val engine = remember {
          CrawlerEngine(
            soundManager = soundManager,
            onGameOver = { finalScore ->
              if (finalScore > highScore) {
                highScore = finalScore
                prefs.edit().putInt("high_score", highScore).apply()
              }
            }
          ).apply {
            this.highScore = highScore
          }
        }

        // Save High Score on change
        LaunchedEffect(highScore) {
          engine.highScore = highScore
          prefs.edit().putInt("high_score", highScore).apply()
        }

        // Save sound & vibration settings
        DisposableEffect(Unit) {
          onDispose {
            prefs.edit()
              .putBoolean("sound_enabled", soundManager.isSoundEnabled)
              .putBoolean("vibration_enabled", soundManager.isVibrationEnabled)
              .apply()
          }
        }

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DeepStarryNight)
        ) {
          when (currentScreen) {
            Screen.TITLE -> {
              TitleScreen(
                highScore = highScore,
                soundManager = soundManager,
                onPlayClicked = {
                  isMultiplayerMode = false
                  engine.isMultiplayer = false
                  engine.reset(customization)
                  currentScreen = Screen.GAME
                },
                onMultiplayerClicked = {
                  currentScreen = Screen.MULTIPLAYER_LOBBY
                },
                onCharacterClicked = {
                  currentScreen = Screen.CHARACTER_SELECTION
                },
                onHowToPlayClicked = {
                  showHowToPlay = true
                },
                onSettingsClicked = {
                  showSettings = true
                }
              )
            }

            Screen.MULTIPLAYER_LOBBY -> {
              BackHandler {
                multiplayerManager.leaveRoom()
                currentScreen = Screen.TITLE
              }
              MultiplayerLobbyScreen(
                multiplayerManager = multiplayerManager,
                soundManager = soundManager,
                onStartGame = {
                  isMultiplayerMode = true
                  engine.isMultiplayer = true
                  engine.reset(customization)
                  currentScreen = Screen.GAME
                },
                onBackClicked = {
                  currentScreen = Screen.TITLE
                }
              )
            }

            Screen.CHARACTER_SELECTION -> {
              BackHandler {
                currentScreen = Screen.TITLE
              }
              CharacterSelectionScreen(
                customization = customization,
                soundManager = soundManager,
                onSaveCustomization = { newCustom ->
                  customization = newCustom
                  prefs.edit()
                    .putString("player_name", newCustom.name)
                    .putInt("player_color_idx", newCustom.colorIndex)
                    .putInt("player_antenna_idx", newCustom.antennaIndex)
                    .putInt("player_eye_idx", newCustom.eyeIndex)
                    .apply()
                },
                onBackClicked = {
                  currentScreen = Screen.TITLE
                }
              )
            }

            Screen.GAME -> {
              BackHandler {
                currentScreen = Screen.TITLE
              }
              GameScreen(
                engine = engine,
                soundManager = soundManager,
                isMultiplayer = isMultiplayerMode,
                onExitToMenu = {
                  currentScreen = Screen.TITLE
                }
              )
            }
          }

          // Modal Dialogs
          if (showHowToPlay) {
            HowToPlayDialog(
              soundManager = soundManager,
              onDismiss = { showHowToPlay = false }
            )
          }

          if (showSettings) {
            SettingsDialog(
              soundManager = soundManager,
              controlType = controlType,
              onControlTypeChanged = { controlType = it },
              onResetHighScore = {
                highScore = 0
                prefs.edit().putInt("high_score", 0).apply()
              },
              onDismiss = { showSettings = false }
            )
          }
        }
      }
    }
  }
}
