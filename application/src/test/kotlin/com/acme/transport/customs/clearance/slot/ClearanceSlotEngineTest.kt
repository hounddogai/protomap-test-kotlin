package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceSlotEngine()
        val item = newClearanceSlot("code-410", "name-69")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
