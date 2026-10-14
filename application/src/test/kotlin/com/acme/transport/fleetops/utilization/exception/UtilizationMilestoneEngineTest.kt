package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationMilestoneEngine(newRegistry())
        val item = newUtilizationMilestone("code-318", "label-224")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
