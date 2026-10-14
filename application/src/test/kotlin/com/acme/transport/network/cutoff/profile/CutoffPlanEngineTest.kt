package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffPlanEngine()
        val item = newCutoffPlan("code-903", "title-177")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
