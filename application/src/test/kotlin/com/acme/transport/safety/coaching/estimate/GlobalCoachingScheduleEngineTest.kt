package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCoachingScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCoachingScheduleEngine()
        val item = newGlobalCoachingSchedule("code-467", "label-341")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
