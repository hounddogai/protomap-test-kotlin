package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCameraEventAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCameraEventAdjustmentEngine()
        val item = newFallbackCameraEventAdjustment("code-566", "label-518")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
