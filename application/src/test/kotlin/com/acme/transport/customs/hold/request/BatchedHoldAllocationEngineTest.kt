package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedHoldAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedHoldAllocationEngine()
        val item = newBatchedHoldAllocation("code-182", "title-361")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
