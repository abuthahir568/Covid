package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundManager
import com.example.model.ControlType
import com.example.ui.theme.*

@Composable
fun HowToPlayDialog(
  soundManager: SoundManager,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(24.dp))
        .border(3.dp, HowToPlayOrange, RoundedCornerShape(24.dp)),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CardBgDark)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "HOW TO PLAY",
            fontFamily = ArcadeFontFamily,
            fontSize = 17.sp,
            color = HowToPlayOrange,
            fontWeight = FontWeight.Bold
          )
          IconButton(
            onClick = {
              soundManager.playClick()
              onDismiss()
            },
            modifier = Modifier.testTag("close_how_to_play")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rule 1: Objective & Food
        RuleItem(
          emoji = "🍬",
          title = "Eat Treats To Grow",
          desc = "Collect scattered colorful bug treats on the neighborhood roads to increase score (+10) and grow longer!"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rule 2: Eat or Be Eaten!
        RuleItem(
          emoji = "⚔️",
          title = "Eat Or Be Eaten!",
          desc = "If you collide with a SMALLER crawler, you eat them (+100 score & 'YUM!'). If you collide with a LARGER worm, you lose a heart!"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rule 3: Obstacles
        RuleItem(
          emoji = "🚌",
          title = "Watch For Obstacles",
          desc = "Avoid crashing into parked Yellow School Buses, fences, and houses to preserve your 3 lives."
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rule 4: Dash Boost
        RuleItem(
          emoji = "⚡",
          title = "Dash Speed Boost",
          desc = "Hold the DASH button on the bottom-right for high-speed evasion or hunting! Watch your energy gauge."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            soundManager.playClick()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = HowToPlayOrange),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("got_it_button")
        ) {
          Text(
            text = "GOT IT! LET'S CRAWL",
            fontFamily = CartoonFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
          )
        }
      }
    }
  }
}

@Composable
private fun RuleItem(emoji: String, title: String, desc: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF0F0C29))
      .padding(12.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(text = emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
    Column {
      Text(
        text = title,
        fontFamily = CartoonFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color(0xFFFFD54F)
      )
      Text(
        text = desc,
        fontFamily = CartoonFontFamily,
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.85f),
        lineHeight = 18.sp
      )
    }
  }
}

@Composable
fun SettingsDialog(
  soundManager: SoundManager,
  controlType: ControlType,
  onControlTypeChanged: (ControlType) -> Unit,
  onResetHighScore: () -> Unit,
  onDismiss: () -> Unit
) {
  var isSound by remember { mutableStateOf(soundManager.isSoundEnabled) }
  var isVib by remember { mutableStateOf(soundManager.isVibrationEnabled) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(24.dp))
        .border(3.dp, SettingsBlue, RoundedCornerShape(24.dp)),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CardBgDark)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SETTINGS",
            fontFamily = ArcadeFontFamily,
            fontSize = 17.sp,
            color = SettingsBlue,
            fontWeight = FontWeight.Bold
          )
          IconButton(
            onClick = {
              soundManager.playClick()
              onDismiss()
            },
            modifier = Modifier.testTag("close_settings")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "8-Bit Sound FX",
            fontFamily = CartoonFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
          )
          Switch(
            checked = isSound,
            onCheckedChange = {
              isSound = it
              soundManager.isSoundEnabled = it
              if (it) soundManager.playClick()
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = SettingsBlue,
              checkedTrackColor = SettingsBlue.copy(alpha = 0.5f)
            ),
            modifier = Modifier.testTag("sound_toggle")
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vibration Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Haptic Vibration",
            fontFamily = CartoonFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
          )
          Switch(
            checked = isVib,
            onCheckedChange = {
              isVib = it
              soundManager.isVibrationEnabled = it
              if (it) soundManager.playClick()
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = SettingsBlue,
              checkedTrackColor = SettingsBlue.copy(alpha = 0.5f)
            ),
            modifier = Modifier.testTag("vibration_toggle")
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control Type
        Text(
          text = "CONTROL SCHEME",
          fontFamily = ArcadeFontFamily,
          fontSize = 12.sp,
          color = Color.LightGray,
          modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val isDpad = controlType == ControlType.DPAD
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isDpad) SettingsBlue else Color(0xFF0F0C29))
              .border(1.dp, if (isDpad) Color.White else Color.DarkGray, RoundedCornerShape(10.dp))
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Virtual D-Pad",
              fontFamily = CartoonFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (isDpad) Color.Black else Color.White
            )
          }

          val isTouch = controlType == ControlType.DRAG_JOYSTICK
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isTouch) SettingsBlue else Color(0xFF0F0C29))
              .border(1.dp, if (isTouch) Color.White else Color.DarkGray, RoundedCornerShape(10.dp))
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Touch & Drag",
              fontFamily = CartoonFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (isTouch) Color.Black else Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset High Score
        OutlinedButton(
          onClick = {
            soundManager.playClick()
            onResetHighScore()
            onDismiss()
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("reset_high_score_button")
        ) {
          Text(
            text = "RESET HIGH SCORE",
            fontFamily = ArcadeFontFamily,
            fontSize = 11.sp,
            color = Color(0xFFFF5252)
          )
        }
      }
    }
  }
}
