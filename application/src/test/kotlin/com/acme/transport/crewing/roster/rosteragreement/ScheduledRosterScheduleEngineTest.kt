package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRosterScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRosterScheduleEngine()
        val item = newScheduledRosterSchedule("code-148", "label-811")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
