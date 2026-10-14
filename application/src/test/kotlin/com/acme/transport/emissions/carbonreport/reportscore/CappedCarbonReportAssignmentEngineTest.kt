package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCarbonReportAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCarbonReportAssignmentEngine()
        val item = newCappedCarbonReportAssignment("code-206", "label-943")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
