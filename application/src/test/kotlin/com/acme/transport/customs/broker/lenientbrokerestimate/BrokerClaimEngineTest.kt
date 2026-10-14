package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerClaimEngine()
        val item = newBrokerClaim("code-758", "name-655")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
