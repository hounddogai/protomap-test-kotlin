package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFreightInvoiceRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFreightInvoiceRollupEngine(newRegistry())
        val item = newGlobalFreightInvoiceRollup("code-168", "name-892")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
