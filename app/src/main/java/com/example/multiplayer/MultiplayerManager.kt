package com.example.multiplayer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.example.model.MultiplayerRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class MultiplayerManager(private val context: Context) {

  private val _currentRoom = MutableStateFlow<MultiplayerRoom?>(null)
  val currentRoom: StateFlow<MultiplayerRoom?> = _currentRoom.asStateFlow()

  private val _activeTab = MutableStateFlow(0) // 0: CREATE ROOM, 1: JOIN ROOM
  val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

  private val _roomCodeInput = MutableStateFlow("")
  val roomCodeInput: StateFlow<String> = _roomCodeInput.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  fun setActiveTab(tabIndex: Int) {
    _activeTab.value = tabIndex
    _errorMessage.value = null
  }

  fun updateRoomCodeInput(input: String) {
    if (input.length <= 4 && input.all { it.isDigit() }) {
      _roomCodeInput.value = input
      _errorMessage.value = null
    }
  }

  fun createRoom(hostName: String = "Player 1 (Host)") {
    // Generate a unique 4-digit code (e.g. 4892)
    val randomCode = String.format("%04d", Random.nextInt(1000, 9999))
    val room = MultiplayerRoom(
      roomCode = randomCode,
      isHost = true,
      hostName = hostName,
      guestName = null,
      hostReady = true,
      guestReady = false,
      isStarted = false
    )
    _currentRoom.value = room
    _errorMessage.value = null
  }

  fun copyRoomCode() {
    val room = _currentRoom.value ?: return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Creepy Crawlers Room Code", room.roomCode)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Room Code ${room.roomCode} copied!", Toast.LENGTH_SHORT).show()
  }

  fun joinRoom(code: String, guestName: String = "Player 2") {
    if (code.length != 4) {
      _errorMessage.value = "Please enter a valid 4-digit room code."
      return
    }
    // Connect to room with specified code
    val room = MultiplayerRoom(
      roomCode = code,
      isHost = false,
      hostName = "Player 1 (Host)",
      guestName = guestName,
      hostReady = true,
      guestReady = true,
      isStarted = false
    )
    _currentRoom.value = room
    _errorMessage.value = null
    Toast.makeText(context, "Connected to Room $code!", Toast.LENGTH_SHORT).show()
  }

  fun simulateGuestPlayerJoin(guestName: String = "Player 2 (Friend)") {
    val room = _currentRoom.value ?: return
    _currentRoom.value = room.copy(
      guestName = guestName,
      guestReady = true
    )
    Toast.makeText(context, "$guestName joined the room!", Toast.LENGTH_SHORT).show()
  }

  fun removeGuestPlayer() {
    val room = _currentRoom.value ?: return
    _currentRoom.value = room.copy(
      guestName = null,
      guestReady = false
    )
  }

  fun leaveRoom() {
    _currentRoom.value = null
    _roomCodeInput.value = ""
    _errorMessage.value = null
  }

  fun startGame(): Boolean {
    val room = _currentRoom.value ?: return false
    if (room.isHost && (room.guestName != null || room.hostReady)) {
      _currentRoom.value = room.copy(isStarted = true)
      return true
    }
    return false
  }
}
