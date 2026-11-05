package com.example.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.MapGenerator
import com.example.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object GameCanvasRenderer {

  fun drawGameWorld(
    scope: DrawScope,
    cameraX: Float,
    cameraY: Float,
    zoomScale: Float,
    obstacles: List<Obstacle>,
    foods: List<FoodItem>,
    trafficCars: List<TrafficCar>,
    player: Crawler,
    coPlayer: Crawler?,
    enemies: List<Crawler>,
    yumPopups: List<YumPopup>,
    pulsePhase: Float
  ) {
    with(scope) {
      val viewW = size.width
      val viewH = size.height

      // Scale so approximately 340 world units width is displayed (exact video proportion!)
      val effectiveScale = (viewW / 340f) * zoomScale

      withTransform({
        translate(left = viewW / 2f, top = viewH / 2f)
        scale(scaleX = effectiveScale, scaleY = effectiveScale, pivot = Offset.Zero)
        translate(left = -cameraX, top = -cameraY)
      }) {
        // 1. Lush Green Lawn Background
        drawRect(
          color = Color(0xFF4CAF50),
          topLeft = Offset(0f, 0f),
          size = Size(MapGenerator.WORLD_WIDTH, MapGenerator.WORLD_HEIGHT)
        )

        // 2. Asphalt Roadways with Yellow Dashes & Zebra Crosswalks
        drawRoadsAndCrosswalks()

        // 3. Neighborhood Buildings (Office towers with (H), houses, playground, trees)
        drawObstacles(obstacles)

        // 4. Moving Traffic Cars
        drawTrafficCars(trafficCars)

        // 5. Scattered Treats / Food Items
        drawFoods(foods, pulsePhase)

        // 6. Enemy Crawlers (Orange tiger worm, purple stars worm)
        for (enemy in enemies) {
          if (enemy.alive) {
            drawCaterpillar(enemy, pulsePhase)
          }
        }

        // 7. Co-Player if active
        coPlayer?.let { co ->
          if (co.alive) {
            drawCaterpillar(co, pulsePhase)
            drawHeadingIndicator(co)
          }
        }

        // 8. Player Caterpillar
        if (player.alive) {
          drawCaterpillar(player, pulsePhase)
          drawHeadingIndicator(player)
        }

        // 9. Floating Comic Popups ("CRUNCH!", "GROWING!", "THAT WAS FIRE")
        drawPopups(yumPopups)
      }
    }
  }

  private fun DrawScope.drawRoadsAndCrosswalks() {
    val roadColor = Color(0xFF263238)
    val yellowLane = Color(0xFFFFD54F)
    val rW = MapGenerator.ROAD_WIDTH
    val halfR = rW / 2f

    // Horizontal Streets
    for (y in MapGenerator.HORIZONTAL_STREETS) {
      drawRect(
        color = roadColor,
        topLeft = Offset(0f, y - halfR),
        size = Size(MapGenerator.WORLD_WIDTH, rW)
      )
      // Dashed yellow line
      var x = 0f
      while (x < MapGenerator.WORLD_WIDTH) {
        drawRect(
          color = yellowLane,
          topLeft = Offset(x, y - 2f),
          size = Size(16f, 4f)
        )
        x += 32f
      }
    }

    // Vertical Streets
    for (x in MapGenerator.VERTICAL_STREETS) {
      drawRect(
        color = roadColor,
        topLeft = Offset(x - halfR, 0f),
        size = Size(rW, MapGenerator.WORLD_HEIGHT)
      )
      // Dashed yellow line
      var y = 0f
      while (y < MapGenerator.WORLD_HEIGHT) {
        drawRect(
          color = yellowLane,
          topLeft = Offset(x - 2f, y),
          size = Size(4f, 16f)
        )
        y += 32f
      }
    }

    // Zebra Crosswalks at Intersections (like in video!)
    val crosswalkWhite = Color.White
    for (x in MapGenerator.VERTICAL_STREETS) {
      for (y in MapGenerator.HORIZONTAL_STREETS) {
        val dist = halfR + 10f

        // North crossing
        drawZebraStripes(horizontal = true, centerX = x, centerY = y - dist, length = rW)
        // South crossing
        drawZebraStripes(horizontal = true, centerX = x, centerY = y + dist, length = rW)
        // West crossing
        drawZebraStripes(horizontal = false, centerX = x - dist, centerY = y, length = rW)
        // East crossing
        drawZebraStripes(horizontal = false, centerX = x + dist, centerY = y, length = rW)
      }
    }
  }

  private fun DrawScope.drawZebraStripes(horizontal: Boolean, centerX: Float, centerY: Float, length: Float) {
    val numBars = 5
    val barColor = Color.White
    val barThickness = 3f
    val barSpan = 14f

    if (horizontal) {
      val startX = centerX - length / 2f + 4f
      val barW = (length - 8f) / numBars
      for (i in 0 until numBars) {
        drawRect(
          color = barColor,
          topLeft = Offset(startX + i * barW + 2f, centerY - barSpan / 2f),
          size = Size(barW - 4f, barSpan)
        )
      }
    } else {
      val startY = centerY - length / 2f + 4f
      val barH = (length - 8f) / numBars
      for (i in 0 until numBars) {
        drawRect(
          color = barColor,
          topLeft = Offset(centerX - barSpan / 2f, startY + i * barH + 2f),
          size = Size(barSpan, barH - 4f)
        )
      }
    }
  }

  private fun DrawScope.drawObstacles(obstacles: List<Obstacle>) {
    val paint = Paint().apply {
      isAntiAlias = true
      textAlign = Paint.Align.CENTER
      typeface = Typeface.DEFAULT_BOLD
    }

    for (obs in obstacles) {
      val b = obs.bounds
      val w = b.width
      val h = b.height

      when (obs.type) {
        ObstacleType.OFFICE_TOWER -> {
          // Drop shadow
          drawRoundRect(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(b.left + 4f, b.top + 6f),
            size = Size(w, h),
            cornerRadius = CornerRadius(4f, 4f)
          )
          // Building base
          drawRoundRect(
            color = obs.color,
            topLeft = Offset(b.left, b.top),
            size = Size(w, h),
            cornerRadius = CornerRadius(4f, 4f)
          )

          // Helipad (H) on rooftop if present
          if (obs.hasHelipad) {
            val padCenter = Offset(b.left + w / 2f, b.top + 28f)
            drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 16f, center = padCenter, style = Stroke(width = 2.5f))
            drawCircle(color = Color(0xFF37474F), radius = 14f, center = padCenter)

            // Letter H
            drawContext.canvas.nativeCanvas.apply {
              paint.textSize = 18f
              paint.color = android.graphics.Color.WHITE
              drawText("H", padCenter.x, padCenter.y + 6f, paint)
            }
          }

          // Slate Windows Grid
          val winStartTop = if (obs.hasHelipad) b.top + 52f else b.top + 16f
          val cols = 3
          val rows = 4
          val winW = (w - 20f) / cols
          val winH = (b.bottom - winStartTop - 14f) / rows

          for (c in 0 until cols) {
            for (r in 0 until rows) {
              drawRect(
                color = Color(0xFFE0F7FA),
                topLeft = Offset(b.left + 8f + c * winW, winStartTop + r * winH),
                size = Size(winW - 4f, winH - 4f)
              )
            }
          }
        }

        ObstacleType.BRICK_BUILDING -> {
          drawRoundRect(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(b.left + 4f, b.top + 5f),
            size = Size(w, h),
            cornerRadius = CornerRadius(4f, 4f)
          )
          drawRoundRect(
            color = obs.color,
            topLeft = Offset(b.left, b.top),
            size = Size(w, h),
            cornerRadius = CornerRadius(4f, 4f)
          )
          // Roof Vents
          drawRect(color = Color(0xFF4E342E), topLeft = Offset(b.left + 14f, b.top + 12f), size = Size(16f, 16f))
          drawRect(color = Color(0xFF4E342E), topLeft = Offset(b.right - 30f, b.top + 12f), size = Size(16f, 16f))
          // Windows
          val cols = 4
          val winW = (w - 20f) / cols
          for (c in 0 until cols) {
            drawRect(
              color = Color(0xFFFFF9C4),
              topLeft = Offset(b.left + 8f + c * winW, b.top + 46f),
              size = Size(winW - 6f, 22f)
            )
          }
        }

        ObstacleType.HOUSE -> {
          // Drop shadow
          drawRoundRect(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(b.left + 3f, b.top + 5f),
            size = Size(w, h),
            cornerRadius = CornerRadius(3f, 3f)
          )
          // Tan wall
          drawRect(
            color = Color(0xFFD7CCC8),
            topLeft = Offset(b.left + 4f, b.top + h * 0.4f),
            size = Size(w - 8f, h * 0.6f)
          )
          // Gabled Roof Triangle
          val roofPath = Path().apply {
            moveTo(b.left, b.top + h * 0.42f)
            lineTo(b.left + w / 2f, b.top)
            lineTo(b.right, b.top + h * 0.42f)
            close()
          }
          drawPath(roofPath, color = obs.color)

          // White Door & Window
          drawRect(
            color = Color.White,
            topLeft = Offset(b.left + w / 2f - 6f, b.bottom - 16f),
            size = Size(12f, 16f)
          )
          drawRect(
            color = Color(0xFF81D4FA),
            topLeft = Offset(b.left + 10f, b.top + h * 0.5f),
            size = Size(10f, 10f)
          )
          drawRect(
            color = Color(0xFF81D4FA),
            topLeft = Offset(b.right - 20f, b.top + h * 0.5f),
            size = Size(10f, 10f)
          )
        }

        ObstacleType.PLAYGROUND -> {
          // Sandbox
          drawRoundRect(
            color = Color(0xFFFFF59D),
            topLeft = Offset(b.left, b.top),
            size = Size(w, h),
            cornerRadius = CornerRadius(6f, 6f)
          )
          // Swing set (Blue A-frame with yellow bar)
          drawLine(color = Color(0xFF1976D2), start = Offset(b.left + 15f, b.top + 20f), end = Offset(b.left + 15f, b.bottom - 20f), strokeWidth = 3f)
          drawLine(color = Color(0xFF1976D2), start = Offset(b.left + 50f, b.top + 20f), end = Offset(b.left + 50f, b.bottom - 20f), strokeWidth = 3f)
          drawLine(color = Color(0xFFFFD54F), start = Offset(b.left + 12f, b.top + 22f), end = Offset(b.left + 53f, b.top + 22f), strokeWidth = 3f)
          // Slide (Red arc)
          drawLine(color = Color(0xFFE53935), start = Offset(b.right - 40f, b.top + 20f), end = Offset(b.right - 15f, b.bottom - 20f), strokeWidth = 4f)
        }

        ObstacleType.TREE -> {
          drawCircle(color = Color.Black.copy(alpha = 0.2f), radius = w / 2f + 2f, center = Offset(b.left + w / 2f + 2f, b.top + h / 2f + 3f))
          drawCircle(color = Color(0xFF2E7D32), radius = w / 2f, center = Offset(b.left + w / 2f, b.top + h / 2f))
          drawCircle(color = Color(0xFF66BB6A), radius = w * 0.32f, center = Offset(b.left + w * 0.42f, b.top + h * 0.42f))
        }
      }
    }
  }

  private fun DrawScope.drawTrafficCars(cars: List<TrafficCar>) {
    for (car in cars) {
      val isHoriz = car.direction == Direction.LEFT || car.direction == Direction.RIGHT
      val w = if (isHoriz) car.length else car.width
      val h = if (isHoriz) car.width else car.length

      if (car.isBus) {
        // Detailed Yellow School Bus from video
        drawRoundRect(
          color = Color.Black.copy(alpha = 0.3f),
          topLeft = Offset(car.x - w / 2f + 2f, car.y - h / 2f + 3f),
          size = Size(w, h),
          cornerRadius = CornerRadius(5f, 5f)
        )
        // Yellow bus body
        drawRoundRect(
          color = Color(0xFFFFB300),
          topLeft = Offset(car.x - w / 2f, car.y - h / 2f),
          size = Size(w, h),
          cornerRadius = CornerRadius(5f, 5f)
        )
        // Black side stripe
        if (isHoriz) {
          drawRect(color = Color.Black, topLeft = Offset(car.x - w / 2f, car.y - 1.5f), size = Size(w, 3f))
        } else {
          drawRect(color = Color.Black, topLeft = Offset(car.x - 1.5f, car.y - h / 2f), size = Size(3f, h))
        }
        // Red flashing roof emergency lights
        drawCircle(color = Color(0xFFFF1744), radius = 2.5f, center = Offset(car.x - w / 2f + 4f, car.y - h / 2f + 3f))
        drawCircle(color = Color(0xFFFF1744), radius = 2.5f, center = Offset(car.x + w / 2f - 4f, car.y - h / 2f + 3f))
        // Windows
        val windColor = Color(0xFF81D4FA)
        if (isHoriz) {
          val winCols = 4
          val winSpan = (w - 10f) / winCols
          for (k in 0 until winCols) {
            drawRect(color = windColor, topLeft = Offset(car.x - w / 2f + 4f + k * winSpan, car.y - h / 2f + 3f), size = Size(winSpan - 3f, h * 0.32f))
          }
        }
      } else {
        // Cars & Taxis
        drawRoundRect(
          color = Color.Black.copy(alpha = 0.25f),
          topLeft = Offset(car.x - w / 2f + 1f, car.y - h / 2f + 2f),
          size = Size(w, h),
          cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
          color = car.color,
          topLeft = Offset(car.x - w / 2f, car.y - h / 2f),
          size = Size(w, h),
          cornerRadius = CornerRadius(4f, 4f)
        )
        // Taxi sign
        if (car.color == Color(0xFFFFEB3B)) {
          drawRoundRect(color = Color.White, topLeft = Offset(car.x - 4f, car.y - 3f), size = Size(8f, 6f), cornerRadius = CornerRadius(1f, 1f))
          drawRect(color = Color.Black, topLeft = Offset(car.x - 2f, car.y - 1f), size = Size(4f, 2f))
        }
        // Windshield
        val windColor = Color(0xFF81D4FA)
        if (isHoriz) {
          val windX = if (car.direction == Direction.RIGHT) car.x + 3f else car.x - 9f
          drawRect(color = windColor, topLeft = Offset(windX, car.y - h / 2f + 2f), size = Size(6f, h - 4f))
        } else {
          val windY = if (car.direction == Direction.DOWN) car.y + 3f else car.y - 9f
          drawRect(color = windColor, topLeft = Offset(car.x - w / 2f + 2f, windY), size = Size(w - 4f, 6f))
        }
      }
    }
  }

  private fun DrawScope.drawFoods(foods: List<FoodItem>, pulsePhase: Float) {
    for (food in foods) {
      val pulse = 1f + sin(pulsePhase + food.id) * 0.15f
      val r = food.radius * pulse

      // Cookie / treat shadow
      drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = r + 1f, center = Offset(food.x + 1f, food.y + 2f))
      // Main treat
      drawCircle(color = food.color, radius = r, center = Offset(food.x, food.y))
      // Cookie chocolate chips
      drawCircle(color = Color(0xFF5D4037), radius = 2.5f, center = Offset(food.x - 3f, food.y - 2f))
      drawCircle(color = Color(0xFF5D4037), radius = 2.5f, center = Offset(food.x + 3f, food.y + 2f))
    }
  }

  private fun DrawScope.drawCaterpillar(c: Crawler, pulsePhase: Float) {
    if (c.invulnerableTimer > 0f && (c.invulnerableTimer * 10).toInt() % 2 == 0) return

    val segCount = c.segments.size
    val r = 16f

    // Draw Segments with spiky/bristled edges (exact look from video!)
    for (i in (segCount - 1) downTo 1) {
      val seg = c.segments[i]
      val taper = (1f - (i.toFloat() / segCount) * 0.18f).coerceAtLeast(0.75f)
      val segR = r * taper

      // Shadow
      drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = segR + 2f, center = Offset(seg.x + 2f, seg.y + 2f))

      // Outer Bristles (small radiating spike circles like in video!)
      val spikes = 8
      for (s in 0 until spikes) {
        val spAngle = (s.toFloat() / spikes) * 2 * PI.toFloat()
        val spX = seg.x + cos(spAngle) * (segR + 3f)
        val spY = seg.y + sin(spAngle) * (segR + 3f)
        drawCircle(color = c.accentColor, radius = 2.5f, center = Offset(spX, spY))
      }

      // Main Segment Circle
      drawCircle(color = c.color, radius = segR, center = Offset(seg.x, seg.y))

      // Segment Center Pattern
      if (c.patternType == "spotted") {
        // Dark brown / orange spot
        drawCircle(color = c.accentColor, radius = segR * 0.45f, center = Offset(seg.x, seg.y))
      } else {
        // Diamond / Star highlight
        drawCircle(color = Color.White.copy(alpha = 0.6f), radius = segR * 0.35f, center = Offset(seg.x, seg.y))
      }
    }

    // Draw Head
    val headR = r * 1.25f
    drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = headR + 2f, center = Offset(c.headX + 2f, c.headY + 2f))
    drawCircle(color = c.color, radius = headR, center = Offset(c.headX, c.headY))

    // Animated Ears (from video: cute bunny/cat/tiger ears that twitch!)
    if (c.hasEars) {
      val earDist = headR * 0.95f
      val earJiggle = sin(c.earTwitchPhase) * 1.8f
      val earAngle1 = c.angle - 0.9f + earJiggle * 0.04f
      val earAngle2 = c.angle + 0.9f - earJiggle * 0.04f

      val ear1 = Offset(c.headX + cos(earAngle1) * earDist, c.headY + sin(earAngle1) * earDist)
      val ear2 = Offset(c.headX + cos(earAngle2) * earDist, c.headY + sin(earAngle2) * earDist)
      val earR = headR * 0.38f

      // Outer ear
      drawCircle(color = c.accentColor, radius = earR, center = ear1)
      drawCircle(color = Color(0xFFFF80AB), radius = earR * 0.55f, center = ear1) // inner pink ear

      drawCircle(color = c.accentColor, radius = earR, center = ear2)
      drawCircle(color = Color(0xFFFF80AB), radius = earR * 0.55f, center = ear2)
    }

    // Antennae
    val antSpread = 0.45f
    val antLength = headR * 1.45f
    val ant1 = Offset(c.headX + cos(c.angle - antSpread) * antLength, c.headY + sin(c.angle - antSpread) * antLength)
    val ant2 = Offset(c.headX + cos(c.angle + antSpread) * antLength, c.headY + sin(c.angle + antSpread) * antLength)

    drawLine(color = c.accentColor, start = Offset(c.headX, c.headY), end = ant1, strokeWidth = 3f)
    drawCircle(color = c.accentColor, radius = 4f, center = ant1)

    drawLine(color = c.accentColor, start = Offset(c.headX, c.headY), end = ant2, strokeWidth = 3f)
    drawCircle(color = c.accentColor, radius = 4f, center = ant2)

    // Dynamic Facial Expressions & Eyes!
    val eyeR = headR * 0.46f
    val eyeDist = headR * 0.42f
    val eye1 = Offset(c.headX + cos(c.angle - 0.58f) * eyeDist, c.headY + sin(c.angle - 0.58f) * eyeDist)
    val eye2 = Offset(c.headX + cos(c.angle + 0.58f) * eyeDist, c.headY + sin(c.angle + 0.58f) * eyeDist)

    when (c.expression) {
      FacialExpression.EATING -> {
        // Joyful squinted eyes: ^ ^
        val squintW = eyeR * 0.85f
        drawLine(color = Color.Black, start = Offset(eye1.x - squintW, eye1.y + 2f), end = Offset(eye1.x, eye1.y - squintW * 0.6f), strokeWidth = 3f)
        drawLine(color = Color.Black, start = Offset(eye1.x, eye1.y - squintW * 0.6f), end = Offset(eye1.x + squintW, eye1.y + 2f), strokeWidth = 3f)

        drawLine(color = Color.Black, start = Offset(eye2.x - squintW, eye2.y + 2f), end = Offset(eye2.x, eye2.y - squintW * 0.6f), strokeWidth = 3f)
        drawLine(color = Color.Black, start = Offset(eye2.x, eye2.y - squintW * 0.6f), end = Offset(eye2.x + squintW, eye2.y + 2f), strokeWidth = 3f)

        // Open chewing mouth with pink tongue
        val mouthPos = Offset(c.headX + cos(c.angle) * (headR * 0.55f), c.headY + sin(c.angle) * (headR * 0.55f))
        drawCircle(color = Color(0xFF3E2723), radius = headR * 0.28f, center = mouthPos)
        drawCircle(color = Color(0xFFFF4081), radius = headR * 0.16f, center = Offset(mouthPos.x, mouthPos.y + 2f))
      }

      FacialExpression.HURT -> {
        // Dizzy X X eyes
        val xSize = eyeR * 0.7f
        drawLine(color = Color.Black, start = Offset(eye1.x - xSize, eye1.y - xSize), end = Offset(eye1.x + xSize, eye1.y + xSize), strokeWidth = 2.5f)
        drawLine(color = Color.Black, start = Offset(eye1.x + xSize, eye1.y - xSize), end = Offset(eye1.x - xSize, eye1.y + xSize), strokeWidth = 2.5f)

        drawLine(color = Color.Black, start = Offset(eye2.x - xSize, eye2.y - xSize), end = Offset(eye2.x + xSize, eye2.y + xSize), strokeWidth = 2.5f)
        drawLine(color = Color.Black, start = Offset(eye2.x + xSize, eye2.y - xSize), end = Offset(eye2.x - xSize, eye2.y + xSize), strokeWidth = 2.5f)

        // Shocked O mouth
        val mouthPos = Offset(c.headX + cos(c.angle) * (headR * 0.55f), c.headY + sin(c.angle) * (headR * 0.55f))
        drawCircle(color = Color.Black, radius = headR * 0.24f, center = mouthPos, style = Stroke(width = 2.5f))
      }

      FacialExpression.BLINKING -> {
        // Horizontal closed eyelid
        val bW = eyeR * 0.85f
        drawLine(color = Color.Black, start = Offset(eye1.x - bW, eye1.y), end = Offset(eye1.x + bW, eye1.y), strokeWidth = 3f)
        drawLine(color = Color.Black, start = Offset(eye2.x - bW, eye2.y), end = Offset(eye2.x + bW, eye2.y), strokeWidth = 3f)
      }

      FacialExpression.DASHING -> {
        // Big determined sparkle eyes
        drawCircle(color = Color.White, radius = eyeR, center = eye1)
        drawCircle(color = Color.Black, radius = eyeR, center = eye1, style = Stroke(width = 2f))
        drawCircle(color = Color.White, radius = eyeR, center = eye2)
        drawCircle(color = Color.Black, radius = eyeR, center = eye2, style = Stroke(width = 2f))

        val pupilShift = 4.5f
        val p1 = Offset(eye1.x + cos(c.angle) * pupilShift, eye1.y + sin(c.angle) * pupilShift)
        val p2 = Offset(eye2.x + cos(c.angle) * pupilShift, eye2.y + sin(c.angle) * pupilShift)

        drawCircle(color = Color.Black, radius = eyeR * 0.58f, center = p1)
        drawCircle(color = Color.Black, radius = eyeR * 0.58f, center = p2)
        // Fiery golden sparkle
        drawCircle(color = Color(0xFFFFD54F), radius = eyeR * 0.28f, center = Offset(p1.x - 1f, p1.y - 1f))
        drawCircle(color = Color(0xFFFFD54F), radius = eyeR * 0.28f, center = Offset(p2.x - 1f, p2.y - 1f))
      }

      FacialExpression.NORMAL -> {
        // Standard big cartoon eyes with pupils looking forward
        drawCircle(color = Color.White, radius = eyeR, center = eye1)
        drawCircle(color = Color.Black, radius = eyeR, center = eye1, style = Stroke(width = 1.5f))

        drawCircle(color = Color.White, radius = eyeR, center = eye2)
        drawCircle(color = Color.Black, radius = eyeR, center = eye2, style = Stroke(width = 1.5f))

        val pupilShift = 3.5f
        val pupil1 = Offset(eye1.x + cos(c.angle) * pupilShift, eye1.y + sin(c.angle) * pupilShift)
        val pupil2 = Offset(eye2.x + cos(c.angle) * pupilShift, eye2.y + sin(c.angle) * pupilShift)

        drawCircle(color = Color.Black, radius = eyeR * 0.52f, center = pupil1)
        drawCircle(color = Color.Black, radius = eyeR * 0.52f, center = pupil2)

        // Shines
        drawCircle(color = Color.White, radius = eyeR * 0.22f, center = Offset(pupil1.x - 1.5f, pupil1.y - 1.5f))
        drawCircle(color = Color.White, radius = eyeR * 0.22f, center = Offset(pupil2.x - 1.5f, pupil2.y - 1.5f))

        // Cute smiling mouth
        val mouthPos = Offset(c.headX + cos(c.angle) * (headR * 0.5f), c.headY + sin(c.angle) * (headR * 0.5f))
        drawCircle(color = Color(0xFF37474F), radius = headR * 0.12f, center = mouthPos)
      }
    }
  }

  private fun DrawScope.drawHeadingIndicator(c: Crawler) {
    // Golden-Yellow Guide Triangle in front of head pointing in the direction of nextDir (exact from video!)
    val arrowDist = 38f
    val tipX = c.headX + c.nextDir.dx * arrowDist
    val tipY = c.headY + c.nextDir.dy * arrowDist

    val perpX = -c.nextDir.dy
    val perpY = c.nextDir.dx
    val baseDist = 18f
    val halfW = 12f

    val baseX = tipX - c.nextDir.dx * baseDist
    val baseY = tipY - c.nextDir.dy * baseDist

    val corner1 = Offset(baseX + perpX * halfW, baseY + perpY * halfW)
    val corner2 = Offset(baseX - perpX * halfW, baseY - perpY * halfW)
    val tip = Offset(tipX, tipY)

    val trianglePath = Path().apply {
      moveTo(tip.x, tip.y)
      lineTo(corner1.x, corner1.y)
      lineTo(corner2.x, corner2.y)
      close()
    }

    // Shadow
    drawPath(trianglePath, color = Color.Black.copy(alpha = 0.35f))
    // Yellow Arrow Fill
    drawPath(trianglePath, color = Color(0xFFFFD54F))
    // Outline
    drawPath(trianglePath, color = Color(0xFFFF8F00), style = Stroke(width = 2f))
  }

  private fun DrawScope.drawPopups(popups: List<YumPopup>) {
    val paint = Paint().apply {
      isAntiAlias = true
      textSize = 32f
      typeface = Typeface.DEFAULT_BOLD
      textAlign = Paint.Align.CENTER
    }

    val native = drawContext.canvas.nativeCanvas
    for (popup in popups) {
      val alphaInt = (popup.alpha * 255).toInt().coerceIn(0, 255)

      // Main Text with black border
      paint.color = android.graphics.Color.argb(alphaInt, 0, 0, 0)
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 5f
      paint.textSize = 32f
      native.drawText(popup.text, popup.x, popup.y, paint)

      paint.color = android.graphics.Color.argb(
        alphaInt,
        (popup.color.red * 255).toInt(),
        (popup.color.green * 255).toInt(),
        (popup.color.blue * 255).toInt()
      )
      paint.style = Paint.Style.FILL
      native.drawText(popup.text, popup.x, popup.y, paint)

      // Subtext (e.g. "THAT WAS FIRE")
      if (popup.subText.isNotBlank()) {
        paint.textSize = 20f
        paint.color = android.graphics.Color.argb(alphaInt, 0, 0, 0)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        native.drawText(popup.subText, popup.x, popup.y + 24f, paint)

        paint.color = android.graphics.Color.argb(alphaInt, 255, 255, 255)
        paint.style = Paint.Style.FILL
        native.drawText(popup.subText, popup.x, popup.y + 24f, paint)
      }
    }
  }
}
