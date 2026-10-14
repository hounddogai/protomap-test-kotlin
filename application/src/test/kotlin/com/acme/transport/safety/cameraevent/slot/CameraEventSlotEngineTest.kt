package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventSlotEngine()
        val item = newCameraEventSlot("code-236", "title-124")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
