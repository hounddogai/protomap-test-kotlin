package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRosterForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRosterForecastEngine()
        val item = newRollingRosterForecast("code-468", "title-996")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
