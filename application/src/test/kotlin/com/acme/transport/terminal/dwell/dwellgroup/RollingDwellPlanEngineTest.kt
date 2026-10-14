package com.acme.transport.terminal.dwell.dwellgroup

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDwellPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDwellPlanEngine()
        val item = newRollingDwellPlan("code-620", "name-454")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
