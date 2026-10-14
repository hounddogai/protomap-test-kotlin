package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeForecastEngine(newRegistry())
        val item = newDisputeForecast("code-949", "label-332")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
