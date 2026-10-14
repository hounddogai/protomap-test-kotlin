package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCapacityMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCapacityMetricEngine()
        val item = newBatchedCapacityMetric("code-61", "title-410")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
