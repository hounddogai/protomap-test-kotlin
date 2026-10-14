package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCameraEventForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCameraEventForecastEngine()
        val item = newPendingCameraEventForecast("code-800", "title-432")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
