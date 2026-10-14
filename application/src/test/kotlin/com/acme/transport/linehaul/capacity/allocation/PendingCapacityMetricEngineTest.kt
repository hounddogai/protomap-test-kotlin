package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCapacityMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCapacityMetricEngine(newRegistry())
        val item = newPendingCapacityMetric("code-495", "title-933")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
