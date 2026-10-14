package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCameraEventForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCameraEventForecastEngine(newRegistry())
        val item = newWeightedCameraEventForecast("code-362", "title-223")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
