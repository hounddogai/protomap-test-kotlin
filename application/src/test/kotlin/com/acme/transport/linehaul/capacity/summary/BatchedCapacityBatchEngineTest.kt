package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCapacityBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCapacityBatchEngine()
        val item = newBatchedCapacityBatch("code-170", "title-598")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
