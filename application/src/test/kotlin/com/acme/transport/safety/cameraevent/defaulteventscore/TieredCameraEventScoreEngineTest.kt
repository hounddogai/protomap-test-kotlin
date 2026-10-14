package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCameraEventScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCameraEventScoreEngine()
        val item = newTieredCameraEventScore("code-268", "title-355")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
