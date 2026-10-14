package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorAuditEngine()
        val item = newDockDoorAudit("code-835", "name-28")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
