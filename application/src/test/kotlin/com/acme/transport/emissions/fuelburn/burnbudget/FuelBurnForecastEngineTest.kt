package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnForecastEngine()
        val item = newFuelBurnForecast("code-371", "title-585")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
