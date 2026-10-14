package com.acme.transport.crewing.availability.scheduledavailabilitytier

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAvailabilitySlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAvailabilitySlotEngine()
        val item = newCappedAvailabilitySlot("code-802", "title-724")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
