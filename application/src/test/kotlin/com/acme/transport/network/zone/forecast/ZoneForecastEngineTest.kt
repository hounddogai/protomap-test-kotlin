package com.acme.transport.network.zone.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneForecastEngine(newRegistry())
        val item = newZoneForecast("code-156", "label-18")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
