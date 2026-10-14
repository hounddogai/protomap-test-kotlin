package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTireMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTireMetricEngine()
        val item = newPendingTireMetric("code-702", "name-477")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
