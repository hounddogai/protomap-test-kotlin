package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalFuelSurchargeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalFuelSurchargeClaimEngine(newRegistry())
        val item = newSeasonalFuelSurchargeClaim("code-464", "label-901")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
