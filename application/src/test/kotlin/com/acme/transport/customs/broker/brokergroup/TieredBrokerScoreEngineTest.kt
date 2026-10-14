package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredBrokerScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredBrokerScoreEngine()
        val item = newTieredBrokerScore("code-406", "title-686")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
