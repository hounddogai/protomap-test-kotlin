package com.acme.transport.freightpay.freightinvoice.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualFreightInvoiceCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualFreightInvoiceCheckpointEngine()
        val item = newManualFreightInvoiceCheckpoint("code-194", "name-689")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
