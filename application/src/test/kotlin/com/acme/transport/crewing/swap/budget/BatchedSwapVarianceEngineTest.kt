package com.acme.transport.crewing.swap.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedSwapVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedSwapVarianceEngine(newRegistry())
        val item = newBatchedSwapVariance("code-395", "name-538")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
