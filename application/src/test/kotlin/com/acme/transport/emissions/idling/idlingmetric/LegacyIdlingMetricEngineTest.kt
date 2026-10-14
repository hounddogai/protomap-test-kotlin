package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyIdlingMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyIdlingMetricEngine(newRegistry())
        val item = newLegacyIdlingMetric("code-536", "label-255")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
