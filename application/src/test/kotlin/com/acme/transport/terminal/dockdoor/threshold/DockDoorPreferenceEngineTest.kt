package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorPreferenceEngine()
        val item = newDockDoorPreference("code-219", "label-793")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
