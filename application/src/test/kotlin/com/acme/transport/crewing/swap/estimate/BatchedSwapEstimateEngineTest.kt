package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedSwapEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedSwapEstimateEngine(newRegistry())
        val item = newBatchedSwapEstimate("code-454", "label-682")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
