package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorMilestoneEngine(newRegistry())
        val item = newDockDoorMilestone("code-733", "title-881")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
