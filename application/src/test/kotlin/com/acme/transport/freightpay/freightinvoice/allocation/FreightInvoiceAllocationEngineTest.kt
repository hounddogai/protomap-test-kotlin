package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceAllocationEngine(newRegistry())
        val item = newFreightInvoiceAllocation("code-273", "name-480")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
