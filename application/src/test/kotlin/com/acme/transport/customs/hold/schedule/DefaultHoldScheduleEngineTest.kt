package com.acme.transport.customs.hold.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultHoldScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultHoldScheduleEngine()
        val item = newDefaultHoldSchedule("code-524", "title-272")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
