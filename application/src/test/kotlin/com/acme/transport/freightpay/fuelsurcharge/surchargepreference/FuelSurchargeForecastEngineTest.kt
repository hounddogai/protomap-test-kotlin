package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeForecastEngine()
        val item = newFuelSurchargeForecast("code-295", "name-728")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
