package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRosterPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRosterPlanEngine()
        val item = newManualRosterPlan("code-72", "title-276")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
