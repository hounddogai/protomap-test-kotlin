package com.acme.transport.safety.cameraevent.eventbundle

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventThresholdEngine()
        val item = newCameraEventThreshold("code-450", "label-443")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
