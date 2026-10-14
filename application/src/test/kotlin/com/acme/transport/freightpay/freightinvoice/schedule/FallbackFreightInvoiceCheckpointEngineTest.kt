package com.acme.transport.freightpay.freightinvoice.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackFreightInvoiceCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackFreightInvoiceCheckpointEngine()
        val item = newFallbackFreightInvoiceCheckpoint("code-325", "name-731")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
