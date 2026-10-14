package com.acme.transport.safety.cameraevent.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCameraEventReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCameraEventReservationEngine()
        val item = newFallbackCameraEventReservation("code-130", "title-173")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
