package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulPlanEngine()
        val item = newBackhaulPlan("code-281", "title-571")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
