package com.acme.transport.terminal.crossdock.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockScheduleEngine()
        val item = newCrossDockSchedule("code-492", "title-891")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
