package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDropScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDropScheduleEngine()
        val item = newRollingDropSchedule("code-194", "label-442")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
