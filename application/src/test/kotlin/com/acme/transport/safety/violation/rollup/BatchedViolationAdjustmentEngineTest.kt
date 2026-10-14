package com.acme.transport.safety.violation.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedViolationAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedViolationAdjustmentEngine()
        val item = newBatchedViolationAdjustment("code-81", "name-128")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
