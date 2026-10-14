package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDockDoorMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDockDoorMappingEngine(newRegistry())
        val item = newScheduledDockDoorMapping("code-515", "name-743")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
