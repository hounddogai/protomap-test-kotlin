package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredBrokerMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredBrokerMetricEngine()
        val item = newTieredBrokerMetric("code-806", "title-191")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
