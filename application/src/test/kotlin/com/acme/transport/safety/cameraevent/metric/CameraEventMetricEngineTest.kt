package com.acme.transport.safety.cameraevent.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventMetricEngine()
        val item = newCameraEventMetric("code-649", "name-99")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
