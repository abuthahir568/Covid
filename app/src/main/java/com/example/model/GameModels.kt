package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class Direction(val dx: Float, val dy: Float, val angle: Float) {
  UP(0f, -1f, -kotlin.math.PI.toFloat() / 2f),
  DOWN(0f, 1f, kotlin.math.PI.toFloat() / 2f),
  LEFT(-1f, 0f, kotlin.math.PI.toFloat()),
  RIGHT(1f, 0f, 0f)
}

enum class ControlType {
  DPAD,
  DRAG_JOYSTICK
}

enum class FacialExpression {
  NORMAL,    // Looking attentively in movement direction
  EATING,    // Joyful squinty eyes (> <) with open happy chewing mouth!
  DASHING,   // Intense determined eyes with sparkles!
  HURT,      // Shocked dizzy spiral eyes (O O)!
  BLINKING   // Cute periodic blink
}

enum class ObstacleType {
  OFFICE_TOWER,   // Blue/slate tall building with windows & helipad (H)
  BRICK_BUILDING, // Brown brick flat roof building with vents
  HOUSE,          // Colorful gabled roof house
  PLAYGROUND,     // Sandbox with swings and slide
  TREE            // Round tree with foliage
}

data class Obstacle(
  val id: Int,
  val type: ObstacleType,
  val bounds: Rect,
  val color: Color,
  val roofType: String = "flat",
  val hasHelipad: Boolean = false,
  val label: String = ""
)

data class TrafficCar(
  val id: Int,
  var x: Float,
  var y: Float,
  var direction: Direction,
  val color: Color,
  val length: Float = 38f,
  val width: Float = 20f,
  val speed: Float = 85f,
  val isBus: Boolean = false,
  var honkCooldown: Float = 0f,
  var alive: Boolean = true
)

data class FoodItem(
  val id: Int,
  val x: Float,
  val y: Float,
  val color: Color,
  val points: Int = 10,
  val sizeBonus: Int = 1,
  val radius: Float = 10f
)

data class YumPopup(
  val id: Long,
  val text: String,
  val subText: String = "",
  var x: Float,
  var y: Float,
  var alpha: Float = 1.0f,
  val color: Color = Color.White
)

data class Crawler(
  val id: String,
  val name: String,
  val isPlayer: Boolean,
  val isHost: Boolean = false,
  var headX: Float,
  var headY: Float,
  var currentDir: Direction = Direction.RIGHT,
  var nextDir: Direction = Direction.RIGHT,
  var angle: Float = 0f,
  var targetAngle: Float = 0f,
  var speed: Float = 145f,
  var baseSpeed: Float = 145f,
  var dashSpeed: Float = 230f,
  var isDashing: Boolean = false,
  var length: Int = 8,
  var color: Color = Color(0xFF8BC34A),       // Olive green from video
  var accentColor: Color = Color(0xFF689F38), // Darker green
  var segments: List<Offset> = emptyList(),
  var trailHistory: MutableList<Offset> = mutableListOf(),
  var alive: Boolean = true,
  var lives: Int = 5,                         // 5 hearts from video!
  var invulnerableTimer: Float = 0f,
  var score: Int = 8,
  var stage: Int = 1,
  // Dynamic Facial Expressions & Ears
  var expression: FacialExpression = FacialExpression.NORMAL,
  var expressionTimer: Float = 0f,
  var hasEars: Boolean = true,                // Cute rounded bunny/cat/tiger ears!
  var earTwitchPhase: Float = 0f,
  // "Low one by one eat" consumption state
  var segmentsToLose: Int = 0,
  var eatCooldown: Float = 0f,
  // Enemy specific
  var aiTurnCooldown: Float = 0f,
  val patternType: String = "spotted"         // "spotted" (orange tiger worm) or "stars" (purple)
)

data class PlayerCustomization(
  val name: String = "Crawler",
  val colorIndex: Int = 0,
  val antennaIndex: Int = 0,
  val eyeIndex: Int = 0,
  val buttonSizeIndex: Int = 1 // 0: Normal, 1: Large, 2: Extra Large
)

data class MultiplayerRoom(
  val roomCode: String,
  val isHost: Boolean,
  val hostName: String,
  var guestName: String?,
  var hostReady: Boolean = true,
  var guestReady: Boolean = false,
  var isStarted: Boolean = false
)
