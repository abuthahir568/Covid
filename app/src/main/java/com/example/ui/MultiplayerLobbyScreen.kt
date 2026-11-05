package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.model.MultiplayerRoom
import com.example.multiplayer.MultiplayerManager
import com.example.ui.theme.*

@Composable
fun MultiplayerLobbyScreen(
  multiplayerManager: MultiplayerManager,
  soundManager: SoundManager,
  onStartGame: () -> Unit,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val room by multiplayerManager.currentRoom.collectAsState()
  val activeTab by multiplayerManager.activeTab.collectAsState()
  val roomCodeInput by multiplayerManager.roomCodeInput.collectAsState()
  val errorMessage by multiplayerManager.errorMessage.collectAsState()
  val focusManager = LocalFocusManager.current

  // Auto-generate room if none exists on entering create tab
  LaunchedEffect(activeTab) {
    if (activeTab == 0 && room == null) {
      multiplayerManager.createRoom()
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "lobby_glow")
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow"
  )

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
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Logo Header
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          CrawlerAvatarPreview(color = CrawlerYellow, accentColor = CrawlerOrange, sizeDp = 28.dp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "CREEPY CRAWLERS",
            fontFamily = CartoonFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = CrawlerYellow
          )
          Spacer(modifier = Modifier.width(6.dp))
          CrawlerAvatarPreview(color = CrawlerPurple, accentColor = CrawlerPink, sizeDp = 28.dp)
        }
      }

      // Main Lobby Card (Matching Image 2!)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 500.dp)
          .shadow(12.dp, RoundedCornerShape(22.dp))
          .border(3.dp, CardBorderPurple, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgDark)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Lobby Title
          Text(
            text = "MULTIPLAYER LOBBY",
            fontFamily = ArcadeFontFamily,
            fontSize = 17.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(18.dp))

          // Room Code Card (with COPY button)
          val displayCode = room?.roomCode ?: "4892"
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF0F0C29))
              .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
              .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "ROOM CODE: $displayCode",
                fontFamily = ArcadeFontFamily,
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )

              // COPY button
              Button(
                onClick = {
                  soundManager.playClick()
                  multiplayerManager.copyRoomCode()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CopyGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                  .height(34.dp)
                  .testTag("copy_room_code_button")
              ) {
                Text(
                  text = "COPY",
                  fontFamily = ArcadeFontFamily,
                  fontSize = 11.sp,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Tab Row: CREATE ROOM vs JOIN ROOM
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF131036)),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            // Tab 1: CREATE ROOM
            Box(
              modifier = Modifier
                .weight(1f)
                .clickable {
                  soundManager.playClick()
                  multiplayerManager.setActiveTab(0)
                }
                .background(
                  if (activeTab == 0) Color(0xFF311B92) else Color.Transparent
                )
                .border(
                  width = if (activeTab == 0) 2.dp else 0.dp,
                  color = if (activeTab == 0) CardBorderPurple else Color.Transparent,
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "CREATE ROOM",
                fontFamily = CartoonFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (activeTab == 0) Color.White else Color.Gray
              )
            }

            // Tab 2: JOIN ROOM
            Box(
              modifier = Modifier
                .weight(1f)
                .clickable {
                  soundManager.playClick()
                  multiplayerManager.setActiveTab(1)
                }
                .background(
                  if (activeTab == 1) Color(0xFF311B92) else Color.Transparent
                )
                .border(
                  width = if (activeTab == 1) 2.dp else 0.dp,
                  color = if (activeTab == 1) CardBorderPurple else Color.Transparent,
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "JOIN ROOM",
                fontFamily = CartoonFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (activeTab == 1) Color.White else Color.Gray
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // If JOIN ROOM tab is active, show input field
          if (activeTab == 1) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = roomCodeInput,
                  onValueChange = { multiplayerManager.updateRoomCodeInput(it) },
                  placeholder = {
                    Text(
                      text = "ENTER 4-DIGIT CODE",
                      fontFamily = ArcadeFontFamily,
                      fontSize = 11.sp,
                      color = Color.Gray
                    )
                  },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                  ),
                  keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    multiplayerManager.joinRoom(roomCodeInput)
                  }),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CardBorderPurple,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0F0C29),
                    unfocusedContainerColor = Color(0xFF0F0C29)
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("room_code_input")
                )

                Button(
                  onClick = {
                    focusManager.clearFocus()
                    soundManager.playClick()
                    multiplayerManager.joinRoom(roomCodeInput)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = CopyGreen),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .height(52.dp)
                    .testTag("join_room_button")
                ) {
                  Text(
                    text = "JOIN",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              errorMessage?.let { err ->
                Text(
                  text = err,
                  fontFamily = CartoonFontFamily,
                  fontSize = 13.sp,
                  color = Color(0xFFFF5252),
                  modifier = Modifier.padding(top = 6.dp)
                )
              }
            }
          }

          // Player List Section (Matching screenshot!)
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF0F0C29).copy(alpha = 0.85f))
              .border(1.dp, Color(0xFF311B92), RoundedCornerShape(14.dp))
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // Player 1 (Host)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              CrawlerAvatarPreview(color = CrawlerYellow, accentColor = CrawlerOrange, sizeDp = 34.dp)
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Player 1 (Host): Ready",
                  fontFamily = CartoonFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp,
                  color = Color(0xFF00E676)
                )
                Text(
                  text = "Yellow Crawler • Host",
                  fontFamily = CartoonFontFamily,
                  fontSize = 12.sp,
                  color = Color.White.copy(alpha = 0.7f)
                )
              }
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Ready",
                tint = Color(0xFF00E676),
                modifier = Modifier.size(24.dp)
              )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            // Player 2 Status
            val hasGuest = room?.guestName != null
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              if (hasGuest) {
                CrawlerAvatarPreview(color = CrawlerPurple, accentColor = CrawlerPink, sizeDp = 34.dp)
              } else {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.4f))
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (hasGuest) "${room?.guestName}: Ready" else "Player 2: Waiting...",
                  fontFamily = CartoonFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp,
                  color = if (hasGuest) Color(0xFF00E676) else Color.White.copy(alpha = 0.6f)
                )
                Text(
                  text = if (hasGuest) "Purple Crawler • Co-Player" else "Share code to invite or add bot",
                  fontFamily = CartoonFontFamily,
                  fontSize = 12.sp,
                  color = Color.White.copy(alpha = 0.5f)
                )
              }

              if (hasGuest) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Ready",
                  tint = Color(0xFF00E676),
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Instant Simulate / Add Friend Bot Helper (Allows seamless testing in 1 click!)
          TextButton(
            onClick = {
              soundManager.playClick()
              if (room?.guestName != null) {
                multiplayerManager.removeGuestPlayer()
              } else {
                multiplayerManager.simulateGuestPlayerJoin()
              }
            },
            modifier = Modifier.testTag("simulate_guest_toggle")
          ) {
            Icon(
              imageVector = if (room?.guestName != null) Icons.Default.Refresh else Icons.Default.PersonAdd,
              contentDescription = null,
              tint = Color(0xFFFFD54F),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (room?.guestName != null) "Remove Player 2" else "+ Add Co-Player (Bot / Practice)",
              fontFamily = CartoonFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFFFFD54F)
            )
          }
        }
      }

      // Bottom Bar with Back Arrow and Glowing START GAME button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 500.dp)
          .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Back Button (Purple rounded with arrow)
        Box(
          modifier = Modifier
            .size(54.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF512DA8))
            .border(2.dp, CardBorderPurple, RoundedCornerShape(16.dp))
            .clickable {
              soundManager.playClick()
              multiplayerManager.leaveRoom()
              onBackClicked()
            }
            .testTag("lobby_back_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Glowing START GAME Button (Green pill button matching Image 2!)
        Box(
          modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .shadow(
              elevation = 8.dp,
              shape = RoundedCornerShape(28.dp),
              ambientColor = Color(0xFF00E676),
              spotColor = Color(0xFF00E676)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF69F0AE).copy(alpha = glowAlpha),
                  Color(0xFF00C853)
                )
              )
            )
            .border(3.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(28.dp))
            .clickable {
              soundManager.playClick()
              if (room?.guestName == null) {
                // Auto add co-player if solo host clicks start
                multiplayerManager.simulateGuestPlayerJoin()
              }
              multiplayerManager.startGame()
              onStartGame()
            }
            .testTag("start_game_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "START GAME",
            fontFamily = ArcadeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF090A1A),
            letterSpacing = 1.sp
          )
        }
      }
    }
  }
}
