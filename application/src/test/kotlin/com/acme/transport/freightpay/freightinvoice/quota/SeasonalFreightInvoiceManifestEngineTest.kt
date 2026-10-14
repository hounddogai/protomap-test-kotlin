package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalFreightInvoiceManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalFreightInvoiceManifestEngine(newRegistry())
        val item = newSeasonalFreightInvoiceManifest("code-516", "label-989")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
