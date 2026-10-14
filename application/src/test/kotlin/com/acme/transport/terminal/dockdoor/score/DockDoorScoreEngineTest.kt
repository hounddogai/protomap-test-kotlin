package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorScoreEngine()
        val item = newDockDoorScore("code-795", "name-104")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
