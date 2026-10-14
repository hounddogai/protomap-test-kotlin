package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyUtilizationSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyUtilizationSlotEngine()
        val item = newLegacyUtilizationSlot("code-556", "label-415")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
