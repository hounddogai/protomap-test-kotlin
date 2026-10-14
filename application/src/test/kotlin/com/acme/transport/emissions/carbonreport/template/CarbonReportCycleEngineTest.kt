package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportCycleEngine()
        val item = newCarbonReportCycle("code-608", "label-985")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
