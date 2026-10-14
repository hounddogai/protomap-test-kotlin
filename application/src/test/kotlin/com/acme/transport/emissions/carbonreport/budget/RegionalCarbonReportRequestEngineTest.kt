package com.acme.transport.emissions.carbonreport.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCarbonReportRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCarbonReportRequestEngine()
        val item = newRegionalCarbonReportRequest("code-926", "title-468")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
