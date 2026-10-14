package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCarbonReportMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCarbonReportMilestoneEngine()
        val item = newPendingCarbonReportMilestone("code-215", "title-741")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
