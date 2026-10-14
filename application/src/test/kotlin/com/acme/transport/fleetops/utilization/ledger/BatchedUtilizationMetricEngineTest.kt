package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedUtilizationMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedUtilizationMetricEngine(newRegistry())
        val item = newBatchedUtilizationMetric("code-995", "title-414")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
