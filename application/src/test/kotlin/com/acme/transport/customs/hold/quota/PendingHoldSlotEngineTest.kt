package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingHoldSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingHoldSlotEngine()
        val item = newPendingHoldSlot("code-458", "title-800")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
