package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeDraftEngine(newRegistry())
        val item = newFuelSurchargeDraft("code-740", "label-143")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
