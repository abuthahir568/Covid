package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundManager
import com.example.game.CrawlerEngine
import com.example.model.Direction
import com.example.ui.theme.*
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs

@Composable
fun GameScreen(
  engine: CrawlerEngine,
  soundManager: SoundManager,
  isMultiplayer: Boolean,
  onExitToMenu: () -> Unit,
  modifier: Modifier = Modifier
) {
  var gameTick by remember { mutableLongStateOf(0L) }
  var isPaused by remember { mutableStateOf(false) }
  var showGameOverDialog by remember { mutableStateOf(false) }
  var isSoundOn by remember { mutableStateOf(soundManager.isSoundEnabled) }
  var buttonSizeIdx by remember { mutableIntStateOf(1) } // 0: M (48dp), 1: L (58dp), 2: XL (70dp)

  // Game Loop
  LaunchedEffect(isPaused, engine.isGameOver) {
    var lastNano = System.nanoTime()
    while (isActive && !isPaused && !engine.isGameOver) {
      withFrameNanos { now ->
        val dt = ((now - lastNano) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
        lastNano = now
        engine.update(dt)
        gameTick++
      }
    }
    if (engine.isGameOver) {
      showGameOverDialog = true
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulsePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2 * PI.toFloat(),
    animationSpec = infiniteRepeatable(animation = tween(1400, easing = LinearEasing)),
    label = "pulse"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // 1. FULLSCREEN SCROLLING CANVAS
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            val dx = dragAmount.x
            val dy = dragAmount.y
            if (abs(dx) > abs(dy)) {
              if (dx > 5f) engine.requestDirection(Direction.RIGHT)
              else if (dx < -5f) engine.requestDirection(Direction.LEFT)
            } else {
              if (dy > 5f) engine.requestDirection(Direction.DOWN)
              else if (dy < -5f) engine.requestDirection(Direction.UP)
            }
          }
        }
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        if (gameTick >= 0) {
          GameCanvasRenderer.drawGameWorld(
            scope = this,
            cameraX = engine.cameraX,
            cameraY = engine.cameraY,
            zoomScale = engine.currentZoom,
            obstacles = engine.obstacles,
            foods = engine.foods,
            trafficCars = engine.trafficCars,
            player = engine.player,
            coPlayer = engine.coPlayer,
            enemies = engine.enemies,
            yumPopups = engine.yumPopups,
            pulsePhase = pulsePhase
          )
        }
      }
    }

    // 2. TOP RETRO HUD (Exact replica from video!)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left Status Pill: [8 🐛 ❤️❤️❤️❤️❤️ [||||]]
        Box(
          modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.72f))
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Length Number
            Text(
              text = "${engine.player.length}",
              fontFamily = ArcadeFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color.White
            )

            // Caterpillar Icon
            Text(text = "🐛", fontSize = 14.sp)

            // 5 Hearts (from video!)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
              for (i in 1..5) {
                Text(
                  text = if (i <= engine.player.lives) "❤️" else "🖤",
                  fontSize = 12.sp
                )
              }
            }

            // Segmented Progress Bar (Level Up indicator)
            VideoSegmentedProgressBar(length = engine.player.length)
          }
        }

        // Right (X) Close / Pause Button (from video!)
        Box(
          modifier = Modifier
            .size(38.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.65f))
            .border(2.dp, Color.White, CircleShape)
            .clickable {
              soundManager.playClick()
              isPaused = true
              engine.isPaused = true
            }
            .testTag("close_game_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Exit / Pause",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }

    // 3. BOTTOM OVERLAY CONTROLS (Exact replica from video!)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Bottom-Left: 4 Direction Buttons (Up, Down, Left, Right)
      VideoDpad(
        onDirectionSelected = { dir ->
          engine.requestDirection(dir)
        },
        sizeLevel = buttonSizeIdx,
        modifier = Modifier.align(Alignment.BottomStart)
      )

      // Bottom-Right: Sound toggle, Button Size control, and Zoom button (from video!)
      Row(
        modifier = Modifier.align(Alignment.BottomEnd),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Button Size Control (M -> L -> XL)
        val sizeLabel = when (buttonSizeIdx) {
          0 -> "M"
          1 -> "L"
          else -> "XL"
        }
        Box(
          modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.65f))
            .border(1.5.dp, Color(0xFF64B5F6).copy(alpha = 0.7f), CircleShape)
            .clickable {
              soundManager.playClick()
              buttonSizeIdx = (buttonSizeIdx + 1) % 3
            }
            .testTag("button_size_control"),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "SIZE",
              fontSize = 7.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White.copy(alpha = 0.7f)
            )
            Text(
              text = sizeLabel,
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF64B5F6)
            )
          }
        }

        // Sound Button
        Box(
          modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape)
            .clickable {
              isSoundOn = !isSoundOn
              soundManager.isSoundEnabled = isSoundOn
              if (isSoundOn) soundManager.playClick()
            }
            .testTag("sound_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            contentDescription = "Toggle Sound",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        // Zoom Button (Matching video bottom-right button!)
        Box(
          modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.65f))
            .border(1.5.dp, Color(0xFFFFD54F).copy(alpha = 0.7f), CircleShape)
            .clickable {
              engine.toggleZoom()
            }
            .testTag("zoom_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CropFree,
            contentDescription = "Zoom Display",
            tint = Color(0xFFFFD54F),
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }

    // Pause Dialog
    if (isPaused) {
      GamePauseDialog(
        onResume = {
          soundManager.playClick()
          isPaused = false
          engine.isPaused = false
        },
        onRestart = {
          soundManager.playClick()
          isPaused = false
          engine.reset()
        },
        onQuit = {
          soundManager.playClick()
          isPaused = false
          onExitToMenu()
        }
      )
    }

    // Game Over Dialog
    if (showGameOverDialog) {
      GameOverDialog(
        score = engine.player.score,
        highScore = engine.highScore,
        onPlayAgain = {
          soundManager.playClick()
          showGameOverDialog = false
          engine.reset()
        },
        onExitToMenu = {
          soundManager.playClick()
          showGameOverDialog = false
          onExitToMenu()
        }
      )
    }
  }
}

@Composable
fun VideoSegmentedProgressBar(length: Int) {
  val totalSegments = 6
  val filledCount = ((length - 8) / 2).coerceIn(0, totalSegments)
  val colors = listOf(
    Color(0xFFFFB74D), // Orange
    Color(0xFFFFD54F), // Yellow
    Color(0xFF81D4FA), // Light Blue
    Color(0xFF26C6DA), // Cyan
    Color(0xFFAB47BC), // Purple
    Color(0xFF00E676)  // Green
  )

  Box(
    modifier = Modifier
      .width(54.dp)
      .height(11.dp)
      .clip(RoundedCornerShape(3.dp))
      .background(Color.DarkGray.copy(alpha = 0.5f))
      .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
      .padding(1.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
      for (i in 0 until totalSegments) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(if (i < filledCount) colors[i] else Color(0xFF1E212D))
        )
      }
    }
  }
}

@Composable
fun VideoDpad(
  onDirectionSelected: (Direction) -> Unit,
  sizeLevel: Int = 1,
  modifier: Modifier = Modifier
) {
  val (keySize, iconSize, spacing) = when (sizeLevel) {
    0 -> Triple(42.dp, 22.dp, 4.dp)
    1 -> Triple(54.dp, 28.dp, 5.dp)
    else -> Triple(66.dp, 34.dp, 6.dp)
  }

  // Exact layout from video: Up, Left, Down, Right rounded buttons
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(spacing)
  ) {
    // UP Button
    DpadKey(
      icon = Icons.Default.PlayArrow,
      rotationDeg = -90f,
      size = keySize,
      iconSize = iconSize,
      onClick = { onDirectionSelected(Direction.UP) },
      tag = "dpad_up"
    )

    // Middle Row: LEFT, DOWN, RIGHT
    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
      DpadKey(
        icon = Icons.Default.PlayArrow,
        rotationDeg = 180f,
        size = keySize,
        iconSize = iconSize,
        onClick = { onDirectionSelected(Direction.LEFT) },
        tag = "dpad_left"
      )
      DpadKey(
        icon = Icons.Default.PlayArrow,
        rotationDeg = 90f,
        size = keySize,
        iconSize = iconSize,
        onClick = { onDirectionSelected(Direction.DOWN) },
        tag = "dpad_down"
      )
      DpadKey(
        icon = Icons.Default.PlayArrow,
        rotationDeg = 0f,
        size = keySize,
        iconSize = iconSize,
        onClick = { onDirectionSelected(Direction.RIGHT) },
        tag = "dpad_right"
      )
    }
  }
}

@Composable
private fun DpadKey(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  rotationDeg: Float,
  size: androidx.compose.ui.unit.Dp,
  iconSize: androidx.compose.ui.unit.Dp,
  onClick: () -> Unit,
  tag: String
) {
  Box(
    modifier = Modifier
      .size(size)
      .shadow(3.dp, RoundedCornerShape(10.dp))
      .clip(RoundedCornerShape(10.dp))
      .background(Color.Black.copy(alpha = 0.62f))
      .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .testTag(tag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = tag,
      tint = Color.White.copy(alpha = 0.85f),
      modifier = Modifier
        .size(iconSize)
        .offset(
          x = if (rotationDeg == 180f) (-1).dp else if (rotationDeg == 0f) 1.dp else 0.dp,
          y = if (rotationDeg == -90f) (-1).dp else if (rotationDeg == 90f) 1.dp else 0.dp
        )
    )
  }
}

@Composable
fun GamePauseDialog(
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onQuit: () -> Unit
) {
  Dialog(onDismissRequest = onResume) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(20.dp))
        .border(2.dp, Color(0xFF7C4DFF), RoundedCornerShape(20.dp)),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1A47))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "GAME PAUSED",
          fontFamily = ArcadeFontFamily,
          fontSize = 17.sp,
          color = Color.White,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onResume,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("resume_button")
        ) {
          Text(text = "RESUME", fontFamily = CartoonFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = onRestart,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("restart_button")
        ) {
          Text(text = "RESTART", fontFamily = CartoonFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
          onClick = onQuit,
          modifier = Modifier.testTag("quit_button")
        ) {
          Text(text = "EXIT TO MENU", fontFamily = CartoonFontFamily, fontSize = 15.sp, color = Color(0xFFFF5252))
        }
      }
    }
  }
}

@Composable
fun GameOverDialog(
  score: Int,
  highScore: Int,
  onPlayAgain: () -> Unit,
  onExitToMenu: () -> Unit
) {
  val isNewHigh = score >= highScore && score > 0

  Dialog(onDismissRequest = onPlayAgain) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(20.dp, RoundedCornerShape(22.dp))
        .border(3.dp, Color(0xFFFF1744), RoundedCornerShape(22.dp)),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1A47))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "GAME OVER",
          fontFamily = ArcadeFontFamily,
          fontSize = 20.sp,
          color = Color(0xFFFF1744),
          fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isNewHigh) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFFFD54F))
              .padding(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Text(
              text = "★ NEW HIGH SCORE! ★",
              fontFamily = ArcadeFontFamily,
              fontSize = 10.sp,
              color = Color.Black
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
          text = "FINAL SCORE",
          fontFamily = CartoonFontFamily,
          fontSize = 14.sp,
          color = Color.LightGray
        )
        Text(
          text = String.format("%,d", score),
          fontFamily = ArcadeFontFamily,
          fontSize = 26.sp,
          color = ScoreGold,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onPlayAgain,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("play_again_button")
        ) {
          Text(text = "PLAY AGAIN", fontFamily = CartoonFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = onExitToMenu,
          shape = RoundedCornerShape(24.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("game_over_menu_button")
        ) {
          Text(text = "MAIN MENU", fontFamily = CartoonFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      }
    }
  }
}

