package com.acme.transport.crewing.swap.group

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapGroupEngine()
        val item = newSwapGroup("code-56", "title-695")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
