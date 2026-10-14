package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFreightInvoiceProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientFreightInvoiceProfileEngine(newRegistry())
        val item = newLenientFreightInvoiceProfile("code-355", "name-380")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
