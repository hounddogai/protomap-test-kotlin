package com.acme.transport.emissions.carbonreport.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCarbonReportLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCarbonReportLedgerEngine()
        val item = newWeightedCarbonReportLedger("code-53", "name-547")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
