package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportGroupEngine()
        val item = newCarbonReportGroup("code-642", "label-376")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
