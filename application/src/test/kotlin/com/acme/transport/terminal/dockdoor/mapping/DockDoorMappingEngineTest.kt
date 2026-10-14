package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorMappingEngine(newRegistry())
        val item = newDockDoorMapping("code-439", "name-212")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
