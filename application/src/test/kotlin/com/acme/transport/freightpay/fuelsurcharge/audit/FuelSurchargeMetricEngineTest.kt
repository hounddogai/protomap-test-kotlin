package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeMetricEngine()
        val item = newFuelSurchargeMetric("code-760", "title-365")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
