package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictEvChargeSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictEvChargeSlotEngine(newRegistry())
        val item = newStrictEvChargeSlot("code-336", "label-901")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
