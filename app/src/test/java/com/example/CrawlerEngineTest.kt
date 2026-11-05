package com.example

import com.example.game.CrawlerEngine
import com.example.game.MapGenerator
import com.example.model.Crawler
import com.example.model.PlayerCustomization
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CrawlerEngineTest {

  @Test
  fun testMapGeneratorCreatesObstaclesAndFoods() {
    val obstacles = MapGenerator.generateObstacles()
    val foods = MapGenerator.generateInitialFood()

    assertTrue("Obstacles should not be empty", obstacles.isNotEmpty())
    assertTrue("Initial foods should have at least 20 items", foods.size >= 20)
  }

  @Test
  fun testCrawlerInitialization() {
    val customization = PlayerCustomization(name = "TestWorm", colorIndex = 1)
    val dummySoundManager = com.example.audio.SoundManager(
      androidx.test.core.app.ApplicationProvider.getApplicationContext()
    )
    var gameOverCalled = false
    val engine = CrawlerEngine(dummySoundManager) { gameOverCalled = true }

    engine.reset(customization)
    assertEquals("TestWorm", engine.player.name)
    assertEquals(5, engine.player.lives)
    assertEquals(8, engine.player.length)
    assertTrue(engine.enemies.size >= 3)
  }
}
