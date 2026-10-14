package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRosterPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRosterPlanEngine(newRegistry())
        val item = newTieredRosterPlan("code-373", "name-924")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
