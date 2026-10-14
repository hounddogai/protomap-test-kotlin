package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDockDoorScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDockDoorScheduleEngine()
        val item = newDefaultDockDoorSchedule("code-545", "name-306")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
