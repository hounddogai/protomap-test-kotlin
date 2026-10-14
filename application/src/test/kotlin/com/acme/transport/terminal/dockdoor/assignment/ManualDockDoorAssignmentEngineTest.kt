package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDockDoorAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDockDoorAssignmentEngine(newRegistry())
        val item = newManualDockDoorAssignment("code-594", "title-356")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
