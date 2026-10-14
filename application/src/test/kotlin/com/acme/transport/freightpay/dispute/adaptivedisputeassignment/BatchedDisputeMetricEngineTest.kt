package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDisputeMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDisputeMetricEngine()
        val item = newBatchedDisputeMetric("code-334", "name-455")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
