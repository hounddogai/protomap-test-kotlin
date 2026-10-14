package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapBatchEngine()
        val item = newSwapBatch("code-600", "label-506")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
