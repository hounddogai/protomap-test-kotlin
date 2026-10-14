package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalFreightInvoiceWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalFreightInvoiceWindowEngine(newRegistry())
        val item = newSeasonalFreightInvoiceWindow("code-897", "name-255")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
