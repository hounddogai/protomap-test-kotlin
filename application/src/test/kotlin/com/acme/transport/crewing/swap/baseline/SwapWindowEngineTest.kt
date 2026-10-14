package com.acme.transport.crewing.swap.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapWindowEngine()
        val item = newSwapWindow("code-882", "label-567")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
