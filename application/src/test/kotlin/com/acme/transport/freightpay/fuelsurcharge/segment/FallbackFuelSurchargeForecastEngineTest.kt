package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackFuelSurchargeForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackFuelSurchargeForecastEngine(newRegistry())
        val item = newFallbackFuelSurchargeForecast("code-457", "name-748")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
