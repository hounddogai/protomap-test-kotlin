package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyIdlingForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyIdlingForecastEngine(newRegistry())
        val item = newLegacyIdlingForecast("code-29", "title-977")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
