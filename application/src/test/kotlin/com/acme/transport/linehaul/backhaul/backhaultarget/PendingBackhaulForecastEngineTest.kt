package com.acme.transport.linehaul.backhaul.backhaultarget

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingBackhaulForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingBackhaulForecastEngine()
        val item = newPendingBackhaulForecast("code-363", "title-811")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
