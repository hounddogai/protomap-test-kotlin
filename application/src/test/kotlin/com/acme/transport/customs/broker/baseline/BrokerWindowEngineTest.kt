package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerWindowEngine()
        val item = newBrokerWindow("code-382", "label-895")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
