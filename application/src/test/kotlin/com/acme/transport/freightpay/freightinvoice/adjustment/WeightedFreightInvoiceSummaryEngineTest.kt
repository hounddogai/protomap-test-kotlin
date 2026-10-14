package com.acme.transport.freightpay.freightinvoice.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFreightInvoiceSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFreightInvoiceSummaryEngine()
        val item = newWeightedFreightInvoiceSummary("code-580", "name-288")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
