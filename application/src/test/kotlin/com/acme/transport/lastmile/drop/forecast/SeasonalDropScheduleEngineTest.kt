package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDropScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDropScheduleEngine()
        val item = newSeasonalDropSchedule("code-329", "label-729")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
