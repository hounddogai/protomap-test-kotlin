package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCameraEventForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCameraEventForecastEngine(newRegistry())
        val item = newBatchedCameraEventForecast("code-203", "name-378")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
