package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredBrokerRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredBrokerRequestEngine()
        val item = newTieredBrokerRequest("code-654", "label-193")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
