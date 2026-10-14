package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeClaimEngine(newRegistry())
        val item = newFuelSurchargeClaim("code-747", "title-616")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
