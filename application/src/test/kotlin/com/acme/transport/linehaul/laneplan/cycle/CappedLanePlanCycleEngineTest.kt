package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLanePlanCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLanePlanCycleEngine()
        val item = newCappedLanePlanCycle("code-853", "title-313")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
