package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceDigestEngine(newRegistry())
        val item = newFreightInvoiceDigest("code-663", "label-487")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
