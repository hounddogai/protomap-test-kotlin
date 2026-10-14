package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionSlotEngine(newRegistry())
        val item = newInspectionSlot("code-394", "name-645")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
