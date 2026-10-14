package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCarbonReportRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCarbonReportRecordEngine()
        val item = newWeightedCarbonReportRecord("code-145", "label-404")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
