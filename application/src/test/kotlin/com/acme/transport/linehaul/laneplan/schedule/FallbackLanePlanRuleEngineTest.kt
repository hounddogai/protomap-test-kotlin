package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackLanePlanRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackLanePlanRuleEngine()
        val item = newFallbackLanePlanRule("code-415", "label-969")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
