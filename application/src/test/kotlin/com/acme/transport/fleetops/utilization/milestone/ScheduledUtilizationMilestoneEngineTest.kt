package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledUtilizationMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledUtilizationMilestoneEngine()
        val item = newScheduledUtilizationMilestone("code-997", "name-630")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
