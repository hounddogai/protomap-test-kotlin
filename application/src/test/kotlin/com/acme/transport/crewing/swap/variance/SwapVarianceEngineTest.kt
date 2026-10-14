package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapVarianceEngine(newRegistry())
        val item = newSwapVariance("code-962", "title-775")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
