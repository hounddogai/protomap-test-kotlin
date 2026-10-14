package com.acme.transport.customs.broker.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerVarianceEngine(newRegistry())
        val item = newBrokerVariance("code-734", "title-533")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
