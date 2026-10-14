package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportMilestoneEngine()
        val item = newCarbonReportMilestone("code-994", "label-234")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
