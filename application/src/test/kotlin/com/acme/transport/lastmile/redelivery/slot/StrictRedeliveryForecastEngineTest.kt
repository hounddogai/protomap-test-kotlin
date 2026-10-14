package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRedeliveryForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRedeliveryForecastEngine()
        val item = newStrictRedeliveryForecast("code-367", "label-688")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
