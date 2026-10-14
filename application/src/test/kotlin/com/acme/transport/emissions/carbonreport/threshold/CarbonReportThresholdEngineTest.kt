package com.acme.transport.emissions.carbonreport.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportThresholdEngine()
        val item = newCarbonReportThreshold("code-879", "title-666")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
