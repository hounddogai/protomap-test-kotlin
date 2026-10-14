package com.acme.transport.fleetops.utilization.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledUtilizationCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledUtilizationCycleEngine()
        val item = newScheduledUtilizationCycle("code-620", "name-559")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
