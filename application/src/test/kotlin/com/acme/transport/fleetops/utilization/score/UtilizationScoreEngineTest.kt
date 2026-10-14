package com.acme.transport.fleetops.utilization.score

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationScoreEngine()
        val item = newUtilizationScore("code-235", "name-619")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
