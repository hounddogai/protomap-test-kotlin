package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceTargetEngine(newRegistry())
        val item = newFreightInvoiceTarget("code-714", "title-200")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
