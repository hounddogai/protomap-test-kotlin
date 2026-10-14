package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportPreferenceEngine()
        val item = newCarbonReportPreference("code-924", "label-735")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
