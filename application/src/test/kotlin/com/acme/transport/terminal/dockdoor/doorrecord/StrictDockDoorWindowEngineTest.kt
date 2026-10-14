package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDockDoorWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDockDoorWindowEngine(newRegistry())
        val item = newStrictDockDoorWindow("code-650", "name-834")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
