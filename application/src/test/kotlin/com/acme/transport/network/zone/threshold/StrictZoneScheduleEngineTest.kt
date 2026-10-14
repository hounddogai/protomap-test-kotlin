package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictZoneScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictZoneScheduleEngine()
        val item = newStrictZoneSchedule("code-831", "label-123")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
