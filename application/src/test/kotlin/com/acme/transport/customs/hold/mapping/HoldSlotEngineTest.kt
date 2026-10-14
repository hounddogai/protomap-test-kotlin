package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldSlotEngine()
        val item = newHoldSlot("code-997", "name-712")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
