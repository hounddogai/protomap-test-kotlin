package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictFreightInvoiceScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictFreightInvoiceScoreEngine()
        val item = newStrictFreightInvoiceScore("code-499", "title-238")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
