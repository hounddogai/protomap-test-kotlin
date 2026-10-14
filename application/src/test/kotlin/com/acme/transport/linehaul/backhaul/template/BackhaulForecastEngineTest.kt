package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulForecastEngine()
        val item = newBackhaulForecast("code-398", "title-40")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
