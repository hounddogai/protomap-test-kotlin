package com.acme.transport.emissions.carbonreport.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCarbonReportAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCarbonReportAssignmentEngine()
        val item = newLenientCarbonReportAssignment("code-425", "title-636")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
