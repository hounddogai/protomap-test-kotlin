package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapScoreEngine()
        val item = newSwapScore("code-195", "name-667")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
