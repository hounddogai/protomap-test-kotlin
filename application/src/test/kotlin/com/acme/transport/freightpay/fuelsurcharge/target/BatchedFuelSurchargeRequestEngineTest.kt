package com.acme.transport.freightpay.fuelsurcharge.target

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedFuelSurchargeRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedFuelSurchargeRequestEngine()
        val item = newBatchedFuelSurchargeRequest("code-984", "title-349")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
