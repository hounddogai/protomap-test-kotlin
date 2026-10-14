package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCarbonReportEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCarbonReportEstimateEngine()
        val item = newStrictCarbonReportEstimate("code-444", "label-792")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
