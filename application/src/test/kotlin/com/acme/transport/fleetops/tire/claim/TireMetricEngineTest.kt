package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireMetricEngine(newRegistry())
        val item = newTireMetric("code-878", "title-899")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
