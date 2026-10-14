package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualIdlingRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualIdlingRuleEngine(newRegistry())
        val item = newManualIdlingRule("code-100", "title-89")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
