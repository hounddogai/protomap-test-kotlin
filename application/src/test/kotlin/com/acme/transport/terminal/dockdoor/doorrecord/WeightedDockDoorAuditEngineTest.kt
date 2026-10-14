package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDockDoorAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDockDoorAuditEngine()
        val item = newWeightedDockDoorAudit("code-358", "label-446")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
