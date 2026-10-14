package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTenderMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTenderMetricEngine(newRegistry())
        val item = newDefaultTenderMetric("code-382", "label-813")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
