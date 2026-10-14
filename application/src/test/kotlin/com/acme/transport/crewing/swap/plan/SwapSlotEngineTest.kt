package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapSlotEngine(newRegistry())
        val item = newSwapSlot("code-699", "title-413")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
