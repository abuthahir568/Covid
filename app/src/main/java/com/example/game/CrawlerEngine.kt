package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundManager
import com.example.model.*
import kotlin.math.*
import kotlin.random.Random

class CrawlerEngine(
  private val soundManager: SoundManager,
  private val onGameOver: (finalScore: Int) -> Unit
) {
  var player = createDefaultPlayer()
  var coPlayer: Crawler? = null

  var enemies = mutableListOf<Crawler>()
  var foods = mutableListOf<FoodItem>()
  var obstacles = listOf<Obstacle>()
  var trafficCars = mutableListOf<TrafficCar>()
  var yumPopups = mutableListOf<YumPopup>()

  // Camera viewport coordinates (centers on player)
  var cameraX: Float = 0f
  var cameraY: Float = 0f
  var zoomLevel: Float = 1.0f
  var currentZoom: Float = 1.0f

  var highScore: Int = 5600
  var isPaused: Boolean = false
  var isGameOver: Boolean = false
  var isMultiplayer: Boolean = false

  private var popupIdCounter = 0L
  private val random = Random(System.currentTimeMillis())

  init {
    reset()
  }

  private fun createDefaultPlayer(): Crawler {
    val startX = 440f
    val startY = 440f
    return Crawler(
      id = "player_1",
      name = "Player 1",
      isPlayer = true,
      isHost = true,
      headX = startX,
      headY = startY,
      currentDir = Direction.RIGHT,
      nextDir = Direction.RIGHT,
      angle = 0f,
      targetAngle = 0f,
      baseSpeed = 145f,
      dashSpeed = 230f,
      speed = 145f,
      length = 8,
      color = Color(0xFF8BC34A),       // Olive green from video
      accentColor = Color(0xFF689F38),
      lives = 5,                         // 5 hearts from video
      score = 8,
      stage = 1
    )
  }

  fun reset(customPlayer: PlayerCustomization? = null) {
    obstacles = MapGenerator.generateObstacles()
    foods = MapGenerator.generateInitialFood().toMutableList()
    trafficCars = MapGenerator.generateTrafficCars().toMutableList()
    yumPopups.clear()
    isGameOver = false
    isPaused = false

    player = createDefaultPlayer()
    customPlayer?.name?.let { if (it.isNotBlank()) player = player.copy(name = it) }
    initializeCrawlerTrail(player)

    cameraX = player.headX
    cameraY = player.headY

    if (isMultiplayer) {
      coPlayer = Crawler(
        id = "player_2",
        name = "Player 2",
        isPlayer = true,
        isHost = false,
        headX = 740f,
        headY = 440f,
        currentDir = Direction.LEFT,
        nextDir = Direction.LEFT,
        angle = PI.toFloat(),
        targetAngle = PI.toFloat(),
        baseSpeed = 145f,
        dashSpeed = 230f,
        speed = 145f,
        length = 8,
        color = Color(0xFFAB47BC), // Purple
        accentColor = Color(0xFFE1BEE7),
        lives = 5,
        score = 8,
        stage = 1
      )
      initializeCrawlerTrail(coPlayer!!)
    } else {
      coPlayer = null
    }

    spawnEnemies()
  }

  private fun spawnEnemies() {
    enemies.clear()

    // 1. Spotted Orange Tiger Worm (from video!)
    val tigerWorm = Crawler(
      id = "enemy_tiger",
      name = "Tiger Worm",
      isPlayer = false,
      headX = 740f,
      headY = 140f,
      currentDir = Direction.RIGHT,
      nextDir = Direction.RIGHT,
      angle = 0f,
      targetAngle = 0f,
      baseSpeed = 110f,
      speed = 110f,
      length = 10,
      color = Color(0xFFFF9800),
      accentColor = Color(0xFFE65100),
      patternType = "spotted"
    )
    initializeCrawlerTrail(tigerWorm)
    enemies.add(tigerWorm)

    // 2. Purple Star Crawler (from video at 00:22)
    val purpleWorm = Crawler(
      id = "enemy_purple",
      name = "Violet",
      isPlayer = false,
      headX = 1040f,
      headY = 740f,
      currentDir = Direction.DOWN,
      nextDir = Direction.DOWN,
      angle = PI.toFloat() / 2f,
      targetAngle = PI.toFloat() / 2f,
      baseSpeed = 115f,
      speed = 115f,
      length = 14,
      color = Color(0xFF9C27B0),
      accentColor = Color(0xFF4A148C),
      patternType = "stars"
    )
    initializeCrawlerTrail(purpleWorm)
    enemies.add(purpleWorm)

    // 3. Small Snack Worm
    val smallWorm = Crawler(
      id = "enemy_small",
      name = "Sprout",
      isPlayer = false,
      headX = 440f,
      headY = 1040f,
      currentDir = Direction.UP,
      nextDir = Direction.UP,
      angle = -PI.toFloat() / 2f,
      targetAngle = -PI.toFloat() / 2f,
      baseSpeed = 120f,
      speed = 120f,
      length = 6,
      color = Color(0xFFFFB300),
      accentColor = Color(0xFFFF6F00),
      patternType = "spotted"
    )
    initializeCrawlerTrail(smallWorm)
    enemies.add(smallWorm)
  }

  private fun initializeCrawlerTrail(c: Crawler) {
    val trail = mutableListOf<Offset>()
    val spacing = 16f
    for (i in 0 until (c.length * 4 + 20)) {
      val dx = cos(c.angle) * (i * spacing / 4)
      val dy = sin(c.angle) * (i * spacing / 4)
      trail.add(Offset(c.headX - dx, c.headY - dy))
    }
    c.trailHistory = trail
    updateCrawlerSegments(c)
  }

  fun update(dt: Float) {
    if (isPaused || isGameOver) return

    val clampedDt = dt.coerceIn(0.001f, 0.05f)

    // Update Player
    updateGridCrawler(player, clampedDt)

    // Smooth camera tracking on player head
    cameraX += (player.headX - cameraX) * (clampedDt * 8f)
    cameraY += (player.headY - cameraY) * (clampedDt * 8f)
    currentZoom += (zoomLevel - currentZoom) * (clampedDt * 7f)

    // Update face expression & ears
    player.earTwitchPhase += clampedDt * 8f
    if (player.expressionTimer > 0f) {
      player.expressionTimer -= clampedDt
      if (player.expressionTimer <= 0f) {
        player.expression = if (player.isDashing) FacialExpression.DASHING else FacialExpression.NORMAL
      }
    } else if (random.nextInt(180) == 0) {
      // Cute periodic blink
      player.expression = FacialExpression.BLINKING
      player.expressionTimer = 0.16f
    }

    // Check Player Evolution (Stage 1 Olive Green -> Stage 2 Cyan at length >= 11, like video at 00:43!)
    checkEvolution(player)

    // Update Co-player if active
    coPlayer?.let { co ->
      co.earTwitchPhase += clampedDt * 8f
      updateGridCrawler(co, clampedDt)
      checkEvolution(co)
    }

    // Update Enemies
    for (enemy in enemies) {
      if (!enemy.alive) continue
      if (enemy.eatCooldown > 0f) {
        enemy.eatCooldown -= clampedDt
      }
      enemy.earTwitchPhase += clampedDt * 7f
      updateAI(enemy, clampedDt)
      updateGridCrawler(enemy, clampedDt)
    }

    // Update Traffic Cars & Buses
    updateTraffic(clampedDt)

    // Collisions
    handleCollisions(player)
    coPlayer?.let { handleCollisions(it) }

    // Update Popups
    val it = yumPopups.iterator()
    while (it.hasNext()) {
      val popup = it.next()
      popup.alpha -= clampedDt * 0.85f
      popup.y -= clampedDt * 22f
      if (popup.alpha <= 0f) {
        it.remove()
      }
    }

    if (player.score > highScore) {
      highScore = player.score
    }
  }

  private fun checkEvolution(p: Crawler) {
    if (p.length >= 11 && p.stage == 1) {
      p.stage = 2
      // Evolution to Cyan Blue from video!
      p.color = Color(0xFF26C6DA)
      p.accentColor = Color(0xFF0097A7)
      soundManager.playYum()
      yumPopups.add(
        YumPopup(
          id = ++popupIdCounter,
          text = "GROWING!",
          subText = "THAT WAS FIRE",
          x = p.headX,
          y = p.headY - 40f,
          color = Color(0xFF00E5FF)
        )
      )
    } else if (p.length >= 16 && p.stage == 2) {
      p.stage = 3
      p.color = Color(0xFFFFD54F) // Golden Crawler
      p.accentColor = Color(0xFFFF8F00)
      soundManager.playYum()
      yumPopups.add(
        YumPopup(
          id = ++popupIdCounter,
          text = "CHEF'S KISS!",
          subText = "LEVEL UP",
          x = p.headX,
          y = p.headY - 40f,
          color = Color(0xFFFFD54F)
        )
      )
    }
  }

  private fun updateGridCrawler(c: Crawler, dt: Float) {
    if (!c.alive) return

    if (c.invulnerableTimer > 0f) {
      c.invulnerableTimer = (c.invulnerableTimer - dt).coerceAtLeast(0f)
    }

    // Find nearest intersection to check if queued turn is valid
    val currentX = c.headX
    val currentY = c.headY

    // Check if close to an intersection where nextDir can be taken
    if (c.nextDir != c.currentDir) {
      val targetHStreet = MapGenerator.HORIZONTAL_STREETS.minByOrNull { abs(it - currentY) } ?: 0f
      val targetVStreet = MapGenerator.VERTICAL_STREETS.minByOrNull { abs(it - currentX) } ?: 0f

      // Turning from vertical to horizontal
      if ((c.currentDir == Direction.UP || c.currentDir == Direction.DOWN) &&
        (c.nextDir == Direction.LEFT || c.nextDir == Direction.RIGHT)) {
        if (abs(currentY - targetHStreet) < 18f) {
          c.headY = targetHStreet // snap smoothly to street center
          c.currentDir = c.nextDir
          c.targetAngle = c.currentDir.angle
        }
      }
      // Turning from horizontal to vertical
      else if ((c.currentDir == Direction.LEFT || c.currentDir == Direction.RIGHT) &&
        (c.nextDir == Direction.UP || c.nextDir == Direction.DOWN)) {
        if (abs(currentX - targetVStreet) < 18f) {
          c.headX = targetVStreet // snap smoothly to street center
          c.currentDir = c.nextDir
          c.targetAngle = c.currentDir.angle
        }
      }
      // Reversal
      else if (c.nextDir.dx == -c.currentDir.dx && c.nextDir.dy == -c.currentDir.dy) {
        c.currentDir = c.nextDir
        c.targetAngle = c.currentDir.angle
      }
    }

    // Smooth angle interpolation
    var diff = c.targetAngle - c.angle
    while (diff < -PI.toFloat()) diff += (2 * PI).toFloat()
    while (diff > PI.toFloat()) diff -= (2 * PI).toFloat()
    c.angle += diff * (14f * dt).coerceIn(0f, 1f)

    // Advance head along current direction
    c.headX += c.currentDir.dx * c.speed * dt
    c.headY += c.currentDir.dy * c.speed * dt

    // Seamless endless road wrapping (roads never end!)
    val worldW = MapGenerator.WORLD_WIDTH
    val worldH = MapGenerator.WORLD_HEIGHT

    if (c.headX < 0f) {
      c.headX += worldW
      c.trailHistory = c.trailHistory.map { Offset(it.x + worldW, it.y) }.toMutableList()
      if (c.isPlayer) cameraX += worldW
    } else if (c.headX > worldW) {
      c.headX -= worldW
      c.trailHistory = c.trailHistory.map { Offset(it.x - worldW, it.y) }.toMutableList()
      if (c.isPlayer) cameraX -= worldW
    }

    if (c.headY < 0f) {
      c.headY += worldH
      c.trailHistory = c.trailHistory.map { Offset(it.x, it.y + worldH) }.toMutableList()
      if (c.isPlayer) cameraY += worldH
    } else if (c.headY > worldH) {
      c.headY -= worldH
      c.trailHistory = c.trailHistory.map { Offset(it.x, it.y - worldH) }.toMutableList()
      if (c.isPlayer) cameraY -= worldH
    }

    // Update history trail buffer
    c.trailHistory.add(0, Offset(c.headX, c.headY))
    val maxHistory = (c.length + 8) * 8
    if (c.trailHistory.size > maxHistory) {
      c.trailHistory.removeAt(c.trailHistory.size - 1)
    }

    updateCrawlerSegments(c)
  }

  private fun updateCrawlerSegments(c: Crawler) {
    val spacing = 18f
    val segments = mutableListOf<Offset>()
    segments.add(Offset(c.headX, c.headY))

    var accumulatedDist = 0f
    var lastPoint = Offset(c.headX, c.headY)
    var currentTargetDist = spacing

    for (p in c.trailHistory) {
      val d = distance(lastPoint, p)
      accumulatedDist += d
      lastPoint = p

      if (accumulatedDist >= currentTargetDist && segments.size < c.length) {
        segments.add(p)
        currentTargetDist += spacing
      }
    }

    while (segments.size < c.length) {
      segments.add(segments.lastOrNull() ?: Offset(c.headX, c.headY))
    }

    c.segments = segments
  }

  private fun updateAI(enemy: Crawler, dt: Float) {
    enemy.aiTurnCooldown += dt
    if (enemy.aiTurnCooldown > 1.2f + random.nextFloat() * 1.5f) {
      enemy.aiTurnCooldown = 0f
      // Pick random next direction
      val options = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
      enemy.nextDir = options.random(random)
    }
  }

  private fun updateTraffic(dt: Float) {
    for (car in trafficCars) {
      if (!car.alive) continue
      car.x += car.direction.dx * car.speed * dt
      car.y += car.direction.dy * car.speed * dt

      if (car.honkCooldown > 0f) {
        car.honkCooldown -= dt
      }

      // Check distance to player for realistic car honk / bus rumble
      val distToPlayer = distance(Offset(car.x, car.y), Offset(player.headX, player.headY))
      if (distToPlayer < 95f && car.honkCooldown <= 0f) {
        car.honkCooldown = 3.0f
        if (car.isBus) {
          soundManager.playBusVroom()
        } else {
          soundManager.playCarHonk()
        }
      }

      // Seamless endless wrap
      if (car.x < 0f) car.x = MapGenerator.WORLD_WIDTH
      if (car.x > MapGenerator.WORLD_WIDTH) car.x = 0f
      if (car.y < 0f) car.y = MapGenerator.WORLD_HEIGHT
      if (car.y > MapGenerator.WORLD_HEIGHT) car.y = 0f
    }
  }

  private fun handleCollisions(p: Crawler) {
    if (!p.alive) return

    val headPos = Offset(p.headX, p.headY)
    val headRadius = 15f

    // 1. Food Treats Collection
    val foodIt = foods.iterator()
    while (foodIt.hasNext()) {
      val food = foodIt.next()
      if (distance(headPos, Offset(food.x, food.y)) < headRadius + food.radius + 6f) {
        foodIt.remove()
        p.score += food.points
        p.length = (p.length + food.sizeBonus).coerceAtMost(70)
        soundManager.playChomp()
        p.expression = FacialExpression.EATING
        p.expressionTimer = 0.35f

        val funWords = listOf("CRUNCH!", "YUM!", "NOM", "TASTY")
        yumPopups.add(
          YumPopup(
            id = ++popupIdCounter,
            text = funWords.random(random),
            x = p.headX,
            y = p.headY - 25f,
            color = Color.White
          )
        )

        // Respawn new treat on a road
        val isHoriz = random.nextBoolean()
        val rx = if (isHoriz) 60f + random.nextFloat() * (MapGenerator.WORLD_WIDTH - 120f) else MapGenerator.VERTICAL_STREETS.random(random)
        val ry = if (isHoriz) MapGenerator.HORIZONTAL_STREETS.random(random) else 60f + random.nextFloat() * (MapGenerator.WORLD_HEIGHT - 120f)
        foods.add(FoodItem(id = (foods.maxOfOrNull { it.id } ?: 0) + 1, x = rx, y = ry, color = food.color))
        break
      }
    }

    // 2. Enemy Crawler Collisions ("low one by one eat")
    for (enemy in enemies) {
      if (!enemy.alive) continue
      val enemyHead = Offset(enemy.headX, enemy.headY)
      val dist = distance(headPos, enemyHead)

      var bodyCollision = false
      for (seg in enemy.segments) {
        if (distance(headPos, seg) < headRadius + 14f) {
          bodyCollision = true
          break
        }
      }

      if (dist < headRadius + 16f || bodyCollision) {
        if (p.length >= enemy.length) {
          // PLAYER EATS ENEMY SEGMENT BY SEGMENT (ONE BY ONE EAT!)
          if (enemy.eatCooldown <= 0f) {
            enemy.eatCooldown = 0.12f // chomp next segment in 120ms
            enemy.length = (enemy.length - 1).coerceAtLeast(0)
            p.length = (p.length + 1).coerceAtMost(70)
            p.score += 20
            soundManager.playCrunch()
            p.expression = FacialExpression.EATING
            p.expressionTimer = 0.35f

            val phrases = listOf("NOM!", "CRUNCH!", "+20", "CHOMP!", "TASTY")
            yumPopups.add(
              YumPopup(
                id = ++popupIdCounter,
                text = phrases.random(random),
                x = enemyHead.x,
                y = enemyHead.y - 25f,
                color = Color(0xFF00E676)
              )
            )

            if (enemy.length <= 1) {
              enemy.alive = false
              soundManager.playYum()
              yumPopups.add(
                YumPopup(
                  id = ++popupIdCounter,
                  text = "SPLIT!",
                  subText = "ALL EATEN!",
                  x = enemyHead.x,
                  y = enemyHead.y - 35f,
                  color = Color(0xFFFFD54F)
                )
              )

              // Respawn new small enemy elsewhere
              val respawnX = MapGenerator.VERTICAL_STREETS.random(random)
              val respawnY = MapGenerator.HORIZONTAL_STREETS.random(random)
              enemy.headX = respawnX
              enemy.headY = respawnY
              enemy.length = 6 + random.nextInt(4)
              enemy.alive = true
              initializeCrawlerTrail(enemy)
            }
          }
        } else {
          // ENEMY IS LARGER -> LOSE HEART!
          if (p.invulnerableTimer <= 0f) {
            p.lives = (p.lives - 1).coerceAtLeast(0)
            p.invulnerableTimer = 2.0f
            p.expression = FacialExpression.HURT
            p.expressionTimer = 0.8f
            soundManager.playHurt()

            yumPopups.add(
              YumPopup(
                id = ++popupIdCounter,
                text = "OUCH!",
                x = p.headX,
                y = p.headY - 30f,
                color = Color(0xFFFF1744)
              )
            )

            if (p.lives <= 0) {
              p.alive = false
              soundManager.playGameOver()
              onGameOver(p.score)
            }
          }
        }
      }
    }

    // 3. Traffic Car / School Bus Interactions (Honks & Crunching!)
    for (car in trafficCars) {
      if (!car.alive) continue
      val carDist = distance(headPos, Offset(car.x, car.y))
      if (carDist < headRadius + car.width / 2f + 4f) {
        if (p.length >= 10) {
          // Player is big enough to crunch & eat the car or school bus!
          car.alive = false
          p.score += if (car.isBus) 80 else 40
          p.length = (p.length + if (car.isBus) 2 else 1).coerceAtMost(70)
          soundManager.playCrunch()
          soundManager.playCarHonk()
          p.expression = FacialExpression.EATING
          p.expressionTimer = 0.45f

          yumPopups.add(
            YumPopup(
              id = ++popupIdCounter,
              text = if (car.isBus) "BUS CRUNCH!" else "CAR CRUNCH!",
              subText = if (car.isBus) "+80 PTS" else "+40 PTS",
              x = car.x,
              y = car.y - 30f,
              color = Color(0xFFFFD54F)
            )
          )
        } else {
          // Player bumps car: car honks horn!
          soundManager.playCarHonk()
          p.headX -= p.currentDir.dx * 18f
          p.headY -= p.currentDir.dy * 18f
        }
      }
    }
  }

  fun requestDirection(dir: Direction, isPlayer1: Boolean = true) {
    val target = if (isPlayer1) player else (coPlayer ?: return)
    target.nextDir = dir
  }

  fun toggleZoom() {
    zoomLevel = when {
      zoomLevel < 0.9f -> 1.0f    // Normal Video Zoom
      zoomLevel in 0.9f..1.15f -> 1.35f // Action Close-Up Zoom
      else -> 0.78f               // Wide Neighborhood View
    }
    soundManager.playClick()
    yumPopups.add(
      YumPopup(
        id = ++popupIdCounter,
        text = "ZOOM!",
        subText = when {
          zoomLevel > 1.2f -> "ACTION"
          zoomLevel < 0.9f -> "WIDE"
          else -> "NORMAL"
        },
        x = player.headX,
        y = player.headY - 35f,
        color = Color(0xFFFFD54F)
      )
    )
  }

  fun triggerZoomBoost() {
    player.speed = player.dashSpeed
    zoomLevel = 1.25f
    soundManager.playDash()
    yumPopups.add(
      YumPopup(
        id = ++popupIdCounter,
        text = "ZOOM!",
        x = player.headX,
        y = player.headY - 30f,
        color = Color(0xFFFFD54F)
      )
    )
  }

  fun endZoomBoost() {
    player.speed = player.baseSpeed
    zoomLevel = 1.0f
  }

  private fun distance(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
  }
}
