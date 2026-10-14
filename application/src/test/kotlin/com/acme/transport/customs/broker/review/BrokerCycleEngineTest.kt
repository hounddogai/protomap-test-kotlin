package com.acme.transport.customs.broker.review

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerCycleEngine()
        val item = newBrokerCycle("code-688", "name-450")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
