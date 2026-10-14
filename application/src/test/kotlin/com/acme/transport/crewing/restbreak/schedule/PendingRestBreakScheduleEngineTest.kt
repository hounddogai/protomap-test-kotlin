package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRestBreakScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRestBreakScheduleEngine(newRegistry())
        val item = newPendingRestBreakSchedule("code-181", "name-296")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
