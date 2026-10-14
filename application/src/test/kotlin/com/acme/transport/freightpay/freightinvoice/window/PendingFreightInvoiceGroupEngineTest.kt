package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingFreightInvoiceGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingFreightInvoiceGroupEngine()
        val item = newPendingFreightInvoiceGroup("code-905", "title-469")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
