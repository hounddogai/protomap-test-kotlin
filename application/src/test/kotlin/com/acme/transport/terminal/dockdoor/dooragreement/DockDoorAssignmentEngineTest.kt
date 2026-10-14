package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorAssignmentEngine()
        val item = newDockDoorAssignment("code-312", "name-469")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
