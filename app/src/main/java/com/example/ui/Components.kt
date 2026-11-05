package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Crawler
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RetroPillButton(
  text: String,
  gradientStart: Color,
  gradientEnd: Color,
  icon: ImageVector? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  testTag: String = "retro_button"
) {
  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier = modifier
      .testTag(testTag)
      .fillMaxWidth()
      .height(58.dp)
      .shadow(elevation = 6.dp, shape = RoundedCornerShape(29.dp), ambientColor = Color.Black, spotColor = Color.Black)
      .clip(RoundedCornerShape(29.dp))
      .background(
        brush = Brush.verticalGradient(
          colors = if (enabled) listOf(gradientStart, gradientEnd) else listOf(Color.Gray, Color.DarkGray)
        )
      )
      .border(width = 3.dp, color = Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(29.dp))
      .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    // Top gloss highlight
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth(0.9f)
        .height(14.dp)
        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .background(Color.White.copy(alpha = 0.35f))
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    ) {
      Text(
        text = text,
        fontFamily = CartoonFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = Color.White,
        modifier = Modifier.padding(end = if (icon != null) 10.dp else 0.dp)
      )
      icon?.let {
        Icon(
          imageVector = it,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(26.dp)
        )
      }
    }
  }
}

@Composable
fun CrawlerAvatarPreview(
  color: Color,
  accentColor: Color,
  modifier: Modifier = Modifier,
  segmentCount: Int = 6,
  sizeDp: Dp = 44.dp
) {
  val infiniteTransition = rememberInfiniteTransition(label = "crawler_wiggle")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2 * PI.toFloat(),
    animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing), repeatMode = RepeatMode.Restart),
    label = "wave"
  )

  Canvas(modifier = modifier.size(sizeDp)) {
    val w = size.width
    val h = size.height
    val r = h * 0.22f
    val headX = w * 0.78f
    val headY = h * 0.5f

    // Draw body segments
    for (i in (segmentCount - 1) downTo 1) {
      val t = i.toFloat() / segmentCount
      val segX = headX - (i * w * 0.12f)
      val segY = headY + sin(wavePhase + i * 0.8f) * (h * 0.12f)

      drawCircle(
        color = Color.Black.copy(alpha = 0.35f),
        radius = r * (1f - t * 0.2f) + 2f,
        center = Offset(segX, segY + 2f)
      )
      drawCircle(
        color = if (i % 2 == 0) color else accentColor,
        radius = r * (1f - t * 0.2f),
        center = Offset(segX, segY)
      )
      // highlight
      drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = r * 0.35f,
        center = Offset(segX - r * 0.2f, segY - r * 0.2f)
      )
    }

    // Head
    drawCircle(
      color = Color.Black.copy(alpha = 0.4f),
      radius = r * 1.25f + 2f,
      center = Offset(headX, headY + 2f)
    )
    drawCircle(
      color = color,
      radius = r * 1.25f,
      center = Offset(headX, headY)
    )

    // Antennae
    drawLine(
      color = accentColor,
      start = Offset(headX + 2f, headY - r * 0.8f),
      end = Offset(headX + 12f, headY - r * 1.8f),
      strokeWidth = 3f
    )
    drawCircle(
      color = accentColor,
      radius = 4f,
      center = Offset(headX + 12f, headY - r * 1.8f)
    )

    // Eyes
    drawCircle(
      color = Color.White,
      radius = r * 0.55f,
      center = Offset(headX + 4f, headY - 3f)
    )
    drawCircle(
      color = Color.Black,
      radius = r * 0.28f,
      center = Offset(headX + 6f, headY - 3f)
    )
    drawCircle(
      color = Color.White,
      radius = r * 0.12f,
      center = Offset(headX + 7f, headY - 4f)
    )
  }
}
