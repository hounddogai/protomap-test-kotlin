package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapMappingEngine()
        val item = newSwapMapping("code-188", "label-923")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
