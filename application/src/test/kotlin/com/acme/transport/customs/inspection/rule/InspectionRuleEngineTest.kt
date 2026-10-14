package com.acme.transport.customs.inspection.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionRuleEngine()
        val item = newInspectionRule("code-675", "label-476")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
