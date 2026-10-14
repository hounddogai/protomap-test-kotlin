package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDockDoorDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDockDoorDraftEngine(newRegistry())
        val item = newPendingDockDoorDraft("code-659", "title-366")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
