package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeMetricEngine(newRegistry())
        val item = newTransitTimeMetric("code-830", "title-585")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
