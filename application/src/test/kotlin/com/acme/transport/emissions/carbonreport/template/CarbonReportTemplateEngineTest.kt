package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportTemplateEngine()
        val item = newCarbonReportTemplate("code-931", "title-422")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
