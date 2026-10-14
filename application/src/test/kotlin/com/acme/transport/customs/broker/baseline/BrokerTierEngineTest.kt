package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerTierEngine()
        val item = newBrokerTier("code-262", "name-335")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
