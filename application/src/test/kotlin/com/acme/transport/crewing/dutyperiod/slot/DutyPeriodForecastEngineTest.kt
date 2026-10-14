package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodForecastEngine(newRegistry())
        val item = newDutyPeriodForecast("code-531", "name-210")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
