package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedFreightInvoiceOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedFreightInvoiceOverrideEngine(newRegistry())
        val item = newBatchedFreightInvoiceOverride("code-814", "label-104")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
