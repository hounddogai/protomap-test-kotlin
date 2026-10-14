package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorThresholdEngine()
        val item = newDockDoorThreshold("code-993", "name-14")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
