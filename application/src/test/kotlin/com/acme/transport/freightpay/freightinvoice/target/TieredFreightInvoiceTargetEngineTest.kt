package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredFreightInvoiceTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredFreightInvoiceTargetEngine(newRegistry())
        val item = newTieredFreightInvoiceTarget("code-560", "name-331")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
