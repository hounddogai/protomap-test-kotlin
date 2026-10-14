package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCrossDockCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCrossDockCycleEngine()
        val item = newBatchedCrossDockCycle("code-984", "label-237")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
