package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.model.*
import kotlin.random.Random

object MapGenerator {
  // Expansive, endless looping neighborhood world
  const val WORLD_WIDTH = 2400f
  const val WORLD_HEIGHT = 3000f
  const val ROAD_WIDTH = 56f

  // Intersecting Street Grid Coordinates (seamlessly wrapping)
  val HORIZONTAL_STREETS = listOf(
    140f, 440f, 740f, 1040f, 1340f, 1640f, 1940f, 2240f, 2540f, 2840f
  )
  val VERTICAL_STREETS = listOf(
    140f, 440f, 740f, 1040f, 1340f, 1640f, 1940f, 2240f
  )

  fun generateObstacles(): List<Obstacle> {
    val list = mutableListOf<Obstacle>()
    var id = 1

    for (row in 0 until HORIZONTAL_STREETS.size - 1) {
      for (col in 0 until VERTICAL_STREETS.size - 1) {
        val blockLeft = VERTICAL_STREETS[col] + ROAD_WIDTH / 2f + 18f
        val blockRight = VERTICAL_STREETS[col + 1] - ROAD_WIDTH / 2f - 18f
        val blockTop = HORIZONTAL_STREETS[row] + ROAD_WIDTH / 2f + 18f
        val blockBottom = HORIZONTAL_STREETS[row + 1] - ROAD_WIDTH / 2f - 18f

        val blockWidth = blockRight - blockLeft
        val blockHeight = blockBottom - blockTop

        val blockType = (row * 3 + col) % 6

        when (blockType) {
          0 -> {
            // Office Towers with Helipad (H) & Slate Windows
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.OFFICE_TOWER,
                bounds = Rect(blockLeft + 10f, blockTop + 10f, blockLeft + 85f, blockTop + 135f),
                color = Color(0xFF546E7A),
                hasHelipad = true,
                label = "Tower A"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.OFFICE_TOWER,
                bounds = Rect(blockRight - 85f, blockTop + 10f, blockRight - 10f, blockTop + 135f),
                color = Color(0xFF37474F),
                hasHelipad = false,
                label = "Tower B"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.TREE,
                bounds = Rect(blockLeft + blockWidth / 2f - 18f, blockTop + 40f, blockLeft + blockWidth / 2f + 18f, blockTop + 76f),
                color = Color(0xFF2E7D32)
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockLeft + 15f, blockBottom - 65f, blockLeft + 75f, blockBottom - 10f),
                color = Color(0xFF8E24AA),
                roofType = "gable"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockRight - 75f, blockBottom - 65f, blockRight - 15f, blockBottom - 10f),
                color = Color(0xFF1E88E5),
                roofType = "gable"
              )
            )
          }

          1 -> {
            // Row of colorful houses
            val houseColors = listOf(
              Color(0xFF6D4C41), Color(0xFFFB8C00), Color(0xFF43A047),
              Color(0xFF212121), Color(0xFFE0E0E0), Color(0xFF7B1FA2)
            )
            val hW = 60f
            val hH = 50f
            var curX = blockLeft + 10f
            var colorIdx = 0
            while (curX + hW <= blockRight - 5f) {
              list.add(
                Obstacle(
                  id = id++,
                  type = ObstacleType.HOUSE,
                  bounds = Rect(curX, blockTop + 12f, curX + hW, blockTop + 12f + hH),
                  color = houseColors[colorIdx % houseColors.size],
                  roofType = "gable"
                )
              )
              list.add(
                Obstacle(
                  id = id++,
                  type = ObstacleType.HOUSE,
                  bounds = Rect(curX, blockBottom - 12f - hH, curX + hW, blockBottom - 12f),
                  color = houseColors[(colorIdx + 2) % houseColors.size],
                  roofType = "gable"
                )
              )
              curX += hW + 18f
              colorIdx++
            }
          }

          2 -> {
            // Brick Building & Playground
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.BRICK_BUILDING,
                bounds = Rect(blockLeft + 15f, blockTop + 15f, blockLeft + 115f, blockTop + 105f),
                color = Color(0xFF8D6E63),
                label = "Apartments"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.PLAYGROUND,
                bounds = Rect(blockRight - 125f, blockTop + 15f, blockRight - 15f, blockTop + 105f),
                color = Color(0xFFFFF9C4),
                label = "Playground"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.TREE,
                bounds = Rect(blockLeft + 40f, blockBottom - 75f, blockLeft + 76f, blockBottom - 39f),
                color = Color(0xFF388E3C)
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockRight - 80f, blockBottom - 70f, blockRight - 15f, blockBottom - 15f),
                color = Color(0xFFFFB300),
                roofType = "gable"
              )
            )
          }

          3 -> {
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockLeft + 15f, blockTop + 20f, blockLeft + 80f, blockTop + 80f),
                color = Color(0xFF1E88E5),
                roofType = "gable"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockLeft + 100f, blockTop + 20f, blockLeft + 165f, blockTop + 80f),
                color = Color(0xFFE53935),
                roofType = "gable"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.OFFICE_TOWER,
                bounds = Rect(blockRight - 85f, blockBottom - 135f, blockRight - 15f, blockBottom - 15f),
                color = Color(0xFF455A64),
                hasHelipad = true,
                label = "HQ"
              )
            )
          }

          4 -> {
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockLeft + 20f, blockTop + 15f, blockLeft + 85f, blockTop + 75f),
                color = Color(0xFF8E24AA),
                roofType = "gable"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockRight - 85f, blockTop + 15f, blockRight - 20f, blockTop + 75f),
                color = Color(0xFFFF9800),
                roofType = "gable"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.TREE,
                bounds = Rect(blockLeft + blockWidth / 2f - 18f, blockTop + blockHeight / 2f - 18f, blockLeft + blockWidth / 2f + 18f, blockTop + blockHeight / 2f + 18f),
                color = Color(0xFF2E7D32)
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockLeft + 20f, blockBottom - 75f, blockLeft + 85f, blockBottom - 15f),
                color = Color(0xFF43A047),
                roofType = "gable"
              )
            )
          }

          else -> {
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.BRICK_BUILDING,
                bounds = Rect(blockLeft + 20f, blockTop + 20f, blockLeft + 110f, blockTop + 100f),
                color = Color(0xFF6D4C41),
                label = "Studio"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.TREE,
                bounds = Rect(blockRight - 55f, blockTop + 30f, blockRight - 15f, blockTop + 70f),
                color = Color(0xFF2E7D32)
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockLeft + 25f, blockBottom - 70f, blockLeft + 85f, blockBottom - 15f),
                color = Color(0xFF1E88E5),
                roofType = "gable"
              )
            )
            list.add(
              Obstacle(
                id = id++,
                type = ObstacleType.HOUSE,
                bounds = Rect(blockRight - 85f, blockBottom - 70f, blockRight - 25f, blockBottom - 15f),
                color = Color(0xFFE53935),
                roofType = "gable"
              )
            )
          }
        }
      }
    }

    return list
  }

  fun generateTrafficCars(): List<TrafficCar> {
    val cars = mutableListOf<TrafficCar>()
    var id = 1

    // Yellow School Buses
    cars.add(TrafficCar(id++, x = 440f, y = 300f, direction = Direction.DOWN, color = Color(0xFFFFB300), length = 56f, width = 24f, speed = 70f, isBus = true))
    cars.add(TrafficCar(id++, x = 1340f, y = 140f, direction = Direction.RIGHT, color = Color(0xFFFFB300), length = 56f, width = 24f, speed = 70f, isBus = true))
    cars.add(TrafficCar(id++, x = 1640f, y = 1500f, direction = Direction.UP, color = Color(0xFFFFB300), length = 56f, width = 24f, speed = 70f, isBus = true))

    // Yellow Taxis
    cars.add(TrafficCar(id++, x = 200f, y = 440f, direction = Direction.RIGHT, color = Color(0xFFFFEB3B), speed = 90f))
    cars.add(TrafficCar(id++, x = 1040f, y = 1200f, direction = Direction.UP, color = Color(0xFFFFEB3B), speed = 95f))
    cars.add(TrafficCar(id++, x = 740f, y = 1800f, direction = Direction.DOWN, color = Color(0xFFFFEB3B), speed = 90f))

    // Blue Sedans
    cars.add(TrafficCar(id++, x = 600f, y = 140f, direction = Direction.LEFT, color = Color(0xFF29B6F6), speed = 85f))
    cars.add(TrafficCar(id++, x = 1200f, y = 1640f, direction = Direction.LEFT, color = Color(0xFF29B6F6), speed = 85f))
    cars.add(TrafficCar(id++, x = 800f, y = 1040f, direction = Direction.RIGHT, color = Color(0xFF1E88E5), speed = 85f))

    // Green Trucks
    cars.add(TrafficCar(id++, x = 440f, y = 1800f, direction = Direction.DOWN, color = Color(0xFF00E676), length = 46f, width = 22f, speed = 75f))
    cars.add(TrafficCar(id++, x = 1940f, y = 740f, direction = Direction.RIGHT, color = Color(0xFF00E676), length = 46f, width = 22f, speed = 75f))

    return cars
  }

  fun generateInitialFood(count: Int = 48): List<FoodItem> {
    val foods = mutableListOf<FoodItem>()
    val colors = listOf(
      Color(0xFFFFB74D), // Cookie orange
      Color(0xFFFFD54F), // Gold treat
      Color(0xFF81D4FA), // Blue candy
      Color(0xFFAED581)  // Green apple
    )
    val random = Random(42)
    for (i in 0 until count) {
      val isHoriz = random.nextBoolean()
      val x: Float
      val y: Float
      if (isHoriz) {
        y = HORIZONTAL_STREETS.random(random)
        x = 60f + random.nextFloat() * (WORLD_WIDTH - 120f)
      } else {
        x = VERTICAL_STREETS.random(random)
        y = 60f + random.nextFloat() * (WORLD_HEIGHT - 120f)
      }
      foods.add(
        FoodItem(
          id = i + 1,
          x = x,
          y = y,
          color = colors[random.nextInt(colors.size)],
          points = 10,
          sizeBonus = 1,
          radius = 10f
        )
      )
    }
    return foods
  }
}
