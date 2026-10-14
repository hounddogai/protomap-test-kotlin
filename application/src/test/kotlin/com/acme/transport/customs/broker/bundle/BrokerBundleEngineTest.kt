package com.acme.transport.customs.broker.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerBundleEngine()
        val item = newBrokerBundle("code-594", "name-131")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
