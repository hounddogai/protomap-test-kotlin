package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveFreightInvoiceAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveFreightInvoiceAgreementEngine(newRegistry())
        val item = newAdaptiveFreightInvoiceAgreement("code-385", "title-127")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
