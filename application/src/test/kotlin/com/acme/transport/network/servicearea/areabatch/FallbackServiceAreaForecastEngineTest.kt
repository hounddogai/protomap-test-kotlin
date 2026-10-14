package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackServiceAreaForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackServiceAreaForecastEngine(newRegistry())
        val item = newFallbackServiceAreaForecast("code-408", "title-678")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
