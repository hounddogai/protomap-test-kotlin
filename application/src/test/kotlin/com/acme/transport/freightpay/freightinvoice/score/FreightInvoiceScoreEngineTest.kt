package com.acme.transport.freightpay.freightinvoice.score

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceScoreEngine()
        val item = newFreightInvoiceScore("code-910", "name-437")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
