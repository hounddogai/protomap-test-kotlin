package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCarbonReportAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCarbonReportAdjustmentEngine(newRegistry())
        val item = newPrimaryCarbonReportAdjustment("code-725", "title-532")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
