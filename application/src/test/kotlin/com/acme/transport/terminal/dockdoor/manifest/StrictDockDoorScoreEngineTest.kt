package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDockDoorScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDockDoorScoreEngine()
        val item = newStrictDockDoorScore("code-724", "label-92")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
