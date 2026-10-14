package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCarbonReportAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCarbonReportAdjustmentEngine(newRegistry())
        val item = newRegionalCarbonReportAdjustment("code-529", "name-122")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
