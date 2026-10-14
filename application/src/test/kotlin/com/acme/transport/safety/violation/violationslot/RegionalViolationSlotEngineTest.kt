package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalViolationSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalViolationSlotEngine(newRegistry())
        val item = newRegionalViolationSlot("code-407", "label-300")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
