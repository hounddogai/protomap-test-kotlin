package com.acme.transport.linehaul.laneplan.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanRuleEngine()
        val item = newLanePlanRule("code-872", "name-599")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
