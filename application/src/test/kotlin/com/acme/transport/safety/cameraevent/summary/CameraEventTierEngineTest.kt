package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventTierEngine()
        val item = newCameraEventTier("code-612", "title-810")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
