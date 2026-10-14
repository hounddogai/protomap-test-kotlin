package com.acme.transport.crewing.swap.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapDraftEngine()
        val item = newSwapDraft("code-269", "name-757")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
