package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCarbonReportAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCarbonReportAgreementEngine(newRegistry())
        val item = newFallbackCarbonReportAgreement("code-808", "title-764")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
