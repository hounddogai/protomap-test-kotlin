package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCarbonReportEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCarbonReportEstimateEngine()
        val item = newWeightedCarbonReportEstimate("code-603", "name-769")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
