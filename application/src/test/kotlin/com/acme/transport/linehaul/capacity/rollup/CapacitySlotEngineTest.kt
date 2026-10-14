package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacitySlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacitySlotEngine(newRegistry())
        val item = newCapacitySlot("code-880", "label-975")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
