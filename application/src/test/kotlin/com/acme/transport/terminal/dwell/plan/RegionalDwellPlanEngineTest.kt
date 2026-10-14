package com.acme.transport.terminal.dwell.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDwellPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDwellPlanEngine()
        val item = newRegionalDwellPlan("code-659", "title-638")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
