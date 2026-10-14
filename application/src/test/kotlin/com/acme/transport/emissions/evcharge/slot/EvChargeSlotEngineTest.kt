package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeSlotEngine(newRegistry())
        val item = newEvChargeSlot("code-15", "name-192")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
