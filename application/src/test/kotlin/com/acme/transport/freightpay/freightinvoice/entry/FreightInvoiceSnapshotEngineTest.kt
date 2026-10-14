package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceSnapshotEngine(newRegistry())
        val item = newFreightInvoiceSnapshot("code-181", "name-582")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
