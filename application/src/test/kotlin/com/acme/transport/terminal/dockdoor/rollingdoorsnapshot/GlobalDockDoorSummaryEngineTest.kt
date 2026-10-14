package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDockDoorSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDockDoorSummaryEngine(newRegistry())
        val item = newGlobalDockDoorSummary("code-697", "label-541")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
