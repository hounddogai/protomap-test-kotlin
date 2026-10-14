package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerYardScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerYardScheduleEngine()
        val item = newManualTrailerYardSchedule("code-986", "label-60")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
