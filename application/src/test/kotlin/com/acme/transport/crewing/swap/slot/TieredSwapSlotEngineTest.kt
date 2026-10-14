package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredSwapSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredSwapSlotEngine(newRegistry())
        val item = newTieredSwapSlot("code-475", "label-746")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
