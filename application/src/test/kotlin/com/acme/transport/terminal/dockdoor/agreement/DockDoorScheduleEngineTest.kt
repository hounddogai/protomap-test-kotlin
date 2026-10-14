package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorScheduleEngine(newRegistry())
        val item = newDockDoorSchedule("code-725", "name-592")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
