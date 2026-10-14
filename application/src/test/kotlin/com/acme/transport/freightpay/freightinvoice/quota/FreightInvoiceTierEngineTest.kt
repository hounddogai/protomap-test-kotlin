package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceTierEngine(newRegistry())
        val item = newFreightInvoiceTier("code-149", "name-365")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
