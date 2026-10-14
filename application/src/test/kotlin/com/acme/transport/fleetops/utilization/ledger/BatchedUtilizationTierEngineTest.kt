package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedUtilizationTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedUtilizationTierEngine()
        val item = newBatchedUtilizationTier("code-311", "label-583")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
