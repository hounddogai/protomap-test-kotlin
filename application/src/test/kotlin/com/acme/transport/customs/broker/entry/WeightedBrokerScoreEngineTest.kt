package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedBrokerScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedBrokerScoreEngine()
        val item = newWeightedBrokerScore("code-656", "label-104")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
