package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class DropScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropScheduleEngine()
        val item = newDropSchedule("code-406", "name-366")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
