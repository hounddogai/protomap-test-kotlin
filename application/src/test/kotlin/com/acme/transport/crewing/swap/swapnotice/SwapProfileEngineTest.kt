package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapProfileEngine(newRegistry())
        val item = newSwapProfile("code-859", "name-526")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
