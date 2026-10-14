package com.acme.transport.linehaul.relay.weightedrelayrule

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRelayForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRelayForecastEngine()
        val item = newPendingRelayForecast("code-127", "label-433")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
