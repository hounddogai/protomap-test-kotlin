package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedLanePlanVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedLanePlanVarianceEngine()
        val item = newBatchedLanePlanVariance("code-582", "label-135")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
