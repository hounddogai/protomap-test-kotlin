package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceCheckpointEngine(newRegistry())
        val item = newFreightInvoiceCheckpoint("code-892", "name-888")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
