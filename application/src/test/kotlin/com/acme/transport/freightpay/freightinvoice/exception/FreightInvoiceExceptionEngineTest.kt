package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceExceptionEngine(newRegistry())
        val item = newFreightInvoiceException("code-604", "title-597")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
