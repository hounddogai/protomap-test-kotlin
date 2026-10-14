package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeSlotEngine(newRegistry())
        val item = newTransitTimeSlot("code-371", "title-332")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
