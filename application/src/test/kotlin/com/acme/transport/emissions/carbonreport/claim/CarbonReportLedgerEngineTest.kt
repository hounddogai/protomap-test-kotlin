package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportLedgerEngine(newRegistry())
        val item = newCarbonReportLedger("code-535", "label-485")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
