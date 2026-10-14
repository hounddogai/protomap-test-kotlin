package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTenderRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTenderRuleEngine()
        val item = newManualTenderRule("code-33", "label-760")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
