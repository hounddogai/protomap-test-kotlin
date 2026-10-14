package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorAdjustmentEngine(newRegistry())
        val item = newDockDoorAdjustment("code-389", "title-478")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
