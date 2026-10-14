package com.acme.transport.emissions.idling.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingPlanEngine()
        val item = newIdlingPlan("code-138", "title-193")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
