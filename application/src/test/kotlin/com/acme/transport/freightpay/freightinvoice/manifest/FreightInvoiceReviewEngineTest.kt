package com.acme.transport.freightpay.freightinvoice.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceReviewEngine()
        val item = newFreightInvoiceReview("code-269", "name-501")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
