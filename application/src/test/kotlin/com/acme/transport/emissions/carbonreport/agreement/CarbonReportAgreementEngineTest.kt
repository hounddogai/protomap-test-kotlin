package com.acme.transport.emissions.carbonreport.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportAgreementEngine()
        val item = newCarbonReportAgreement("code-845", "title-440")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
