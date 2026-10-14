package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDefectEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDefectEntryEngine()
        val item = newScheduledDefectEntry("code-461", "title-374")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
