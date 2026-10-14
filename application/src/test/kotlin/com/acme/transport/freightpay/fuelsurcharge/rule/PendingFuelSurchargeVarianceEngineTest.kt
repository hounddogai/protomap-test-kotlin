package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingFuelSurchargeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingFuelSurchargeVarianceEngine(newRegistry())
        val item = newPendingFuelSurchargeVariance("code-878", "title-168")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
