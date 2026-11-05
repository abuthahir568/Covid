package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.audio.SoundManager
import com.example.model.PlayerCustomization
import com.example.ui.theme.*

@Composable
fun CharacterSelectionScreen(
  customization: PlayerCustomization,
  soundManager: SoundManager,
  onSaveCustomization: (PlayerCustomization) -> Unit,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedColorIndex by remember { mutableIntStateOf(customization.colorIndex) }
  var selectedAntennaIndex by remember { mutableIntStateOf(customization.antennaIndex) }
  var selectedEyeIndex by remember { mutableIntStateOf(customization.eyeIndex) }
  var playerName by remember { mutableStateOf(customization.name) }

  val colorPalettes = listOf(
    Pair("Golden Sun", Pair(CrawlerYellow, CrawlerOrange)),
    Pair("Toxic Lime", Pair(CrawlerGreen, Color(0xFF00B0FF))),
    Pair("Magma Red", Pair(CrawlerRed, CrawlerOrange)),
    Pair("Cosmic Violet", Pair(CrawlerPurple, CrawlerPink)),
    Pair("Electric Cyan", Pair(CrawlerBlue, CrawlerYellow)),
    Pair("Bubblegum Pink", Pair(CrawlerPink, Color(0xFFFF80AB)))
  )

  val antennaNames = listOf("Classic Round", "Star Sparks", "Feeler Horns", "Curly Sprout")
  val eyeNames = listOf("Big Cute Eyes", "Retro Googly", "Cool Pupils", "Wide Sparkle")

  val (currentColor, currentAccent) = colorPalettes[selectedColorIndex].second

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DeepStarryNight)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Navigation Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            soundManager.playClick()
            onBackClicked()
          },
          modifier = Modifier.testTag("char_select_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
        Text(
          text = "CHARACTER SELECTION",
          fontFamily = ArcadeFontFamily,
          fontSize = 15.sp,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 8.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Live Animated Preview Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(8.dp, RoundedCornerShape(20.dp))
          .border(2.dp, CardBorderPurple, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgDark)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "LIVE PREVIEW",
            fontFamily = ArcadeFontFamily,
            fontSize = 12.sp,
            color = ScoreGold
          )

          Spacer(modifier = Modifier.height(16.dp))

          CrawlerAvatarPreview(
            color = currentColor,
            accentColor = currentAccent,
            segmentCount = 8,
            sizeDp = 80.dp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = playerName.ifEmpty { "Crawler" },
            fontFamily = CartoonFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Custom Name Field
      OutlinedTextField(
        value = playerName,
        onValueChange = { if (it.length <= 15) playerName = it },
        label = { Text("Player Name", fontFamily = CartoonFontFamily, color = Color.LightGray) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CardBorderPurple,
          unfocusedBorderColor = Color.DarkGray,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedContainerColor = CardBgDark,
          unfocusedContainerColor = CardBgDark
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("player_name_input")
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Skin Color Selector
      Text(
        text = "CRAWLER COLOR",
        fontFamily = ArcadeFontFamily,
        fontSize = 13.sp,
        color = Color.White,
        modifier = Modifier.align(Alignment.Start)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        colorPalettes.forEachIndexed { index, (name, colors) ->
          val isSelected = selectedColorIndex == index
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(colors.first)
              .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.5f),
                shape = CircleShape
              )
              .clickable {
                soundManager.playClick()
                selectedColorIndex = index
              }
              .testTag("color_option_$index"),
            contentAlignment = Alignment.Center
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Antenna Style Selector
      Text(
        text = "ANTENNA STYLE",
        fontFamily = ArcadeFontFamily,
        fontSize = 13.sp,
        color = Color.White,
        modifier = Modifier.align(Alignment.Start)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        antennaNames.forEachIndexed { index, title ->
          val isSelected = selectedAntennaIndex == index
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) Color(0xFF512DA8) else CardBgDark)
              .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFFFD54F) else Color.DarkGray,
                shape = RoundedCornerShape(10.dp)
              )
              .clickable {
                soundManager.playClick()
                selectedAntennaIndex = index
              }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = title,
              fontFamily = CartoonFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = if (isSelected) Color.White else Color.Gray,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Save & Back Button
      RetroPillButton(
        text = "CONFIRM & PLAY",
        gradientStart = PlayGreenLight,
        gradientEnd = PlayGreenDark,
        onClick = {
          soundManager.playClick()
          onSaveCustomization(
            PlayerCustomization(
              name = playerName.ifBlank { "Crawler" },
              colorIndex = selectedColorIndex,
              antennaIndex = selectedAntennaIndex,
              eyeIndex = selectedEyeIndex
            )
          )
          onBackClicked()
        },
        testTag = "confirm_customization_button"
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
