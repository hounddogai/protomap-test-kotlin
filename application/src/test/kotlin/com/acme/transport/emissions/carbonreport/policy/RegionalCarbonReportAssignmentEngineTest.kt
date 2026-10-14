package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCarbonReportAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCarbonReportAssignmentEngine()
        val item = newRegionalCarbonReportAssignment("code-265", "name-613")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
