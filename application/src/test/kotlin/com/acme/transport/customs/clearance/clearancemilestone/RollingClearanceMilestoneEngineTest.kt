package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingClearanceMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingClearanceMilestoneEngine()
        val item = newRollingClearanceMilestone("code-390", "label-34")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
