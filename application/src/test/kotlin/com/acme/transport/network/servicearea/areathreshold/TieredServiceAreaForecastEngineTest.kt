package com.acme.transport.network.servicearea.areathreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredServiceAreaForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredServiceAreaForecastEngine()
        val item = newTieredServiceAreaForecast("code-643", "label-80")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
