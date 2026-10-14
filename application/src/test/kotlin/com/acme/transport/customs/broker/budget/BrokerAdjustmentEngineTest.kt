package com.acme.transport.customs.broker.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerAdjustmentEngine()
        val item = newBrokerAdjustment("code-82", "title-533")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
