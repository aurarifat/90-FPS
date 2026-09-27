package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.GameProfile
import com.example.model.FpsMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("90 FPS BOOSTER", appName)
  }

  @Test
  fun `fps mode mappings`() {
    assertEquals(FpsMode.FPS_60, FpsMode.fromFps(60))
    assertEquals(FpsMode.FPS_90, FpsMode.fromFps(90))
    assertEquals(FpsMode.FPS_120, FpsMode.fromFps(120))
    assertEquals(FpsMode.AUTO, FpsMode.fromFps(0))
  }

  @Test
  fun `game profile default values`() {
    val profile = GameProfile(packageName = "com.test.game", appName = "Test Game")
    assertEquals(90, profile.targetFps)
    assertTrue(profile.lockMinAndMaxRate)
    assertTrue(profile.autoActivate)
  }
}
