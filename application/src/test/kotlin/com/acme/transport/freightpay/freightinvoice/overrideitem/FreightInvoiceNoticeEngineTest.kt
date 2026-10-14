package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceNoticeEngine(newRegistry())
        val item = newFreightInvoiceNotice("code-161", "name-390")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
