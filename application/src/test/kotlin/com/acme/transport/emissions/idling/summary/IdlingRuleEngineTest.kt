package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingRuleEngine(newRegistry())
        val item = newIdlingRule("code-538", "title-66")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
