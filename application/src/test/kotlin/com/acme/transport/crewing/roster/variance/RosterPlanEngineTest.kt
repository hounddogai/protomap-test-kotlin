package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterPlanEngine()
        val item = newRosterPlan("code-9", "title-454")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
