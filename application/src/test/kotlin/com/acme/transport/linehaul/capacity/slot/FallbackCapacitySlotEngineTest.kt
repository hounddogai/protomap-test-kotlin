package com.acme.transport.linehaul.capacity.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCapacitySlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCapacitySlotEngine()
        val item = newFallbackCapacitySlot("code-742", "label-906")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
