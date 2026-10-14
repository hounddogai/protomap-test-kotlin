package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedInspectionSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedInspectionSlotEngine(newRegistry())
        val item = newCappedInspectionSlot("code-991", "name-144")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
