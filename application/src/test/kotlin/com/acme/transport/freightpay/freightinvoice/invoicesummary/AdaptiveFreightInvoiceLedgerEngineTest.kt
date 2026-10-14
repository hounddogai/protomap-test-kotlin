package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveFreightInvoiceLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveFreightInvoiceLedgerEngine(newRegistry())
        val item = newAdaptiveFreightInvoiceLedger("code-270", "title-579")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
