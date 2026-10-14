package com.acme.transport.safety.nearmiss.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledNearMissScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledNearMissScheduleEngine()
        val item = newScheduledNearMissSchedule("code-320", "label-579")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
