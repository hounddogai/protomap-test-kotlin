package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorPlanEngine(newRegistry())
        val item = newDockDoorPlan("code-408", "name-874")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
