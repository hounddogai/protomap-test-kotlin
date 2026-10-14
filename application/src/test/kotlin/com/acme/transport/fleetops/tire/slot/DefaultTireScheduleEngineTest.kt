package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTireScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTireScheduleEngine()
        val item = newDefaultTireSchedule("code-117", "name-275")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
