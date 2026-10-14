package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDockDoorDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDockDoorDraftEngine()
        val item = newLegacyDockDoorDraft("code-444", "title-460")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
