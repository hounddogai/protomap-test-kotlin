package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultFreightInvoiceBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultFreightInvoiceBatchEngine(newRegistry())
        val item = newDefaultFreightInvoiceBatch("code-499", "name-775")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
