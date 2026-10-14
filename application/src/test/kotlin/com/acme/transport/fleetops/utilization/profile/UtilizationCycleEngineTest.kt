package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationCycleEngine()
        val item = newUtilizationCycle("code-527", "label-687")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
