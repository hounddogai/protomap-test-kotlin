package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryMetricEngine(newRegistry())
        val item = newRedeliveryMetric("code-977", "title-313")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
