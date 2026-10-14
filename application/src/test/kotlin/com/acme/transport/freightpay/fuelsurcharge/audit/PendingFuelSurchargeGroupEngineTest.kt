package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingFuelSurchargeGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingFuelSurchargeGroupEngine(newRegistry())
        val item = newPendingFuelSurchargeGroup("code-287", "label-409")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
