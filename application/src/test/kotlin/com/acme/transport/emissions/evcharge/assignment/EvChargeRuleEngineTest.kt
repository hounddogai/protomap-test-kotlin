package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeRuleEngine(newRegistry())
        val item = newEvChargeRule("code-570", "name-556")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
