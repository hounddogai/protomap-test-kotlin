package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapOverrideEngine(newRegistry())
        val item = newSwapOverride("code-944", "name-746")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
