package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackSwapGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackSwapGroupEngine(newRegistry())
        val item = newFallbackSwapGroup("code-29", "label-535")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
