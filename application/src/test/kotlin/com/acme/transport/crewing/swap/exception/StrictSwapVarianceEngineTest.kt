package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictSwapVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictSwapVarianceEngine(newRegistry())
        val item = newStrictSwapVariance("code-940", "title-519")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
