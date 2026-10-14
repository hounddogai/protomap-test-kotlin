package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultEvChargeSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultEvChargeSlotEngine(newRegistry())
        val item = newDefaultEvChargeSlot("code-567", "title-7")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
