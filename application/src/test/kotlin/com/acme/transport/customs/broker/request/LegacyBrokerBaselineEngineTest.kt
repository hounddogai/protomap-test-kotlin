package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBrokerBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyBrokerBaselineEngine()
        val item = newLegacyBrokerBaseline("code-967", "name-29")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
