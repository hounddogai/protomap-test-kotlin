package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryBrokerTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryBrokerTierEngine(newRegistry())
        val item = newPrimaryBrokerTier("code-558", "label-474")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
