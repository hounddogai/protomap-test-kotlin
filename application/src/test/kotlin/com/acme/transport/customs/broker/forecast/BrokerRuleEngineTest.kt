package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerRuleEngine()
        val item = newBrokerRule("code-383", "name-71")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
