package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerGroupEngine()
        val item = newBrokerGroup("code-377", "name-93")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
