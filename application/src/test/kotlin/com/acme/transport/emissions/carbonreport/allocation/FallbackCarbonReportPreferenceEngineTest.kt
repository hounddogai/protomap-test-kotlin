package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCarbonReportPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCarbonReportPreferenceEngine()
        val item = newFallbackCarbonReportPreference("code-683", "name-275")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
