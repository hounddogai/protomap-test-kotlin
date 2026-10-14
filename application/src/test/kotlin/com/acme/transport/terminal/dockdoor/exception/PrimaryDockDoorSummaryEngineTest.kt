package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDockDoorSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDockDoorSummaryEngine(newRegistry())
        val item = newPrimaryDockDoorSummary("code-31", "title-795")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
