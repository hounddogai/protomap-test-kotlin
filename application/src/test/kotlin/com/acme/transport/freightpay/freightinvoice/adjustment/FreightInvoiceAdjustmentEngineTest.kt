package com.acme.transport.freightpay.freightinvoice.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceAdjustmentEngine()
        val item = newFreightInvoiceAdjustment("code-384", "name-598")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
