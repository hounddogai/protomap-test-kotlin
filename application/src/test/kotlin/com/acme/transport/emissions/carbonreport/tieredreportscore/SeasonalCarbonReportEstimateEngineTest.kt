package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCarbonReportEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCarbonReportEstimateEngine(newRegistry())
        val item = newSeasonalCarbonReportEstimate("code-758", "name-820")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
