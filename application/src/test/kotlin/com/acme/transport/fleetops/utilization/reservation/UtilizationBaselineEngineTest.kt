package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationBaselineEngine()
        val item = newUtilizationBaseline("code-976", "name-435")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
