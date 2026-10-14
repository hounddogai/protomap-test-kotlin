package com.acme.transport.terminal.dockdoor.doorscore

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDockDoorScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDockDoorScoreEngine()
        val item = newPendingDockDoorScore("code-405", "title-216")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
