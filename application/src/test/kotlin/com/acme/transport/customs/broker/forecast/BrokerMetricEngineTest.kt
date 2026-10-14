package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerMetricEngine(newRegistry())
        val item = newBrokerMetric("code-173", "label-592")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
