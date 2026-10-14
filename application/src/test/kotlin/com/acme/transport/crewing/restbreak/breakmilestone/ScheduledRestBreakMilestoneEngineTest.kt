package com.acme.transport.crewing.restbreak.breakmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRestBreakMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRestBreakMilestoneEngine()
        val item = newScheduledRestBreakMilestone("code-927", "name-827")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
