package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceBatchEngine()
        val item = newFreightInvoiceBatch("code-425", "title-578")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
