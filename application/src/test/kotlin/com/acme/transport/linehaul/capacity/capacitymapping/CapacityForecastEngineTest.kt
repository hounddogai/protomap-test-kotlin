package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityForecastEngine()
        val item = newCapacityForecast("code-230", "name-82")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
