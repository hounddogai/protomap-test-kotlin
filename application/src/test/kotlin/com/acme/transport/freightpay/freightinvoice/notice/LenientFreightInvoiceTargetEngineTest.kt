package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFreightInvoiceTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientFreightInvoiceTargetEngine(newRegistry())
        val item = newLenientFreightInvoiceTarget("code-128", "name-247")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
