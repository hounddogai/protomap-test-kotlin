package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerDigestEngine(newRegistry())
        val item = newBrokerDigest("code-307", "label-438")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
