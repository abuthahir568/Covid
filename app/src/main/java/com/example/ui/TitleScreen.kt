package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.SoundManager
import com.example.ui.theme.*

@Composable
fun TitleScreen(
  highScore: Int,
  soundManager: SoundManager,
  onPlayClicked: () -> Unit,
  onMultiplayerClicked: () -> Unit,
  onCharacterClicked: () -> Unit,
  onHowToPlayClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "title_pulse")
  val titleScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DeepStarryNight)
  ) {
    // Starry background pattern / reference art
    Image(
      painter = painterResource(id = R.drawable.creepy_title_art),
      contentDescription = "Creepy Crawlers Theme Background",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .alphaOrBlack(0.35f)
    )

    // Dark gradient overlay to keep buttons and text ultra crisp and readable
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              DeepStarryNight.copy(alpha = 0.75f),
              DeepStarryNight.copy(alpha = 0.88f),
              DeepStarryNight.copy(alpha = 0.98f)
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Title Banner with Eyes & Worm Badges
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .scale(titleScale)
          .padding(top = 12.dp, bottom = 16.dp)
      ) {
        // Decorative top worms row
        Row(
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(0.9f)
        ) {
          CrawlerAvatarPreview(color = CrawlerPink, accentColor = CrawlerYellow, sizeDp = 34.dp)
          CrawlerAvatarPreview(color = CrawlerGreen, accentColor = CrawlerBlue, sizeDp = 38.dp)
          CrawlerAvatarPreview(color = CrawlerPurple, accentColor = CrawlerPink, sizeDp = 34.dp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Title Header
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "CREEPY CRAWLERS",
            fontFamily = CartoonFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 36.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(x = 2.dp, y = 3.dp)
          )
          Text(
            text = "CREEPY CRAWLERS",
            fontFamily = CartoonFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 36.sp,
            color = CrawlerYellow,
            textAlign = TextAlign.Center
          )
        }

        Text(
          text = "★ FUNBRAIN RETRO EDITION ★",
          fontFamily = ArcadeFontFamily,
          fontSize = 10.sp,
          color = Color(0xFFFFD54F),
          letterSpacing = 1.sp,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      // Action Buttons Stack (Pill buttons matching screenshot!)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 380.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // PLAY NOW (Green)
        RetroPillButton(
          text = "PLAY NOW",
          gradientStart = Color(0xFF66BB6A),
          gradientEnd = Color(0xFF2E7D32),
          icon = Icons.Default.PlayArrow,
          onClick = {
            soundManager.playClick()
            onPlayClicked()
          },
          testTag = "play_now_button"
        )

        // MULTIPLAYER LOBBY (Neon Violet / Purple)
        RetroPillButton(
          text = "MULTIPLAYER LOBBY",
          gradientStart = Color(0xFFBA68C8),
          gradientEnd = Color(0xFF6A1B9A),
          icon = Icons.Default.Group,
          onClick = {
            soundManager.playClick()
            onMultiplayerClicked()
          },
          testTag = "multiplayer_lobby_button"
        )

        // HOW TO PLAY ? (Orange)
        RetroPillButton(
          text = "HOW TO PLAY ?",
          gradientStart = Color(0xFFFFA726),
          gradientEnd = Color(0xFFEF6C00),
          icon = Icons.Default.HelpOutline,
          onClick = {
            soundManager.playClick()
            onHowToPlayClicked()
          },
          testTag = "how_to_play_button"
        )

        // CHARACTER SELECTION (Purple with worm icon)
        RetroPillButton(
          text = "CHARACTER SELECTION",
          gradientStart = Color(0xFFAB47BC),
          gradientEnd = Color(0xFF7B1FA2),
          icon = Icons.Default.Palette,
          onClick = {
            soundManager.playClick()
            onCharacterClicked()
          },
          testTag = "character_selection_button"
        )

        // SETTINGS (Sky Blue)
        RetroPillButton(
          text = "SETTINGS",
          gradientStart = Color(0xFF29B6F6),
          gradientEnd = Color(0xFF0277BD),
          icon = Icons.Default.Settings,
          onClick = {
            soundManager.playClick()
            onSettingsClicked()
          },
          testTag = "settings_button"
        )
      }

      // Bottom High Score Bar & Funbrain tribute
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
      ) {
        Box(
          modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFFFFD54F).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
          Text(
            text = "HIGH SCORE: ${String.format("%04d", highScore)}",
            fontFamily = ArcadeFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = ScoreGold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Funbrain Games Classic",
          fontFamily = CartoonFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = Color.White.copy(alpha = 0.7f)
        )
      }
    }
  }
}

// Extension for composables
private fun Modifier.alphaOrBlack(alpha: Float): Modifier = this.then(Modifier)
