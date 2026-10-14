package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportEstimateEngine(newRegistry())
        val item = newCarbonReportEstimate("code-595", "title-347")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
