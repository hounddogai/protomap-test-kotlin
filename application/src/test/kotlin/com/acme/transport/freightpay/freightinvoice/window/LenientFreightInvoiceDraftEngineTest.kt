package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFreightInvoiceDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientFreightInvoiceDraftEngine(newRegistry())
        val item = newLenientFreightInvoiceDraft("code-2", "label-118")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
