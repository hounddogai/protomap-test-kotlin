package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDockDoorAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDockDoorAuditEngine(newRegistry())
        val item = newPrimaryDockDoorAudit("code-357", "label-314")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
