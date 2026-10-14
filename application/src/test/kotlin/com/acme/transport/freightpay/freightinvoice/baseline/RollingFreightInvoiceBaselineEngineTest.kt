package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFreightInvoiceBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFreightInvoiceBaselineEngine()
        val item = newRollingFreightInvoiceBaseline("code-836", "title-987")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
