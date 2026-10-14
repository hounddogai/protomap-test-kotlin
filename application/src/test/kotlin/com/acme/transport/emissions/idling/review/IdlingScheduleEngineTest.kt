package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingScheduleEngine()
        val item = newIdlingSchedule("code-917", "name-14")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
