package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedEvChargeScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedEvChargeScoreEngine()
        val item = newBatchedEvChargeScore("code-567", "name-141")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
