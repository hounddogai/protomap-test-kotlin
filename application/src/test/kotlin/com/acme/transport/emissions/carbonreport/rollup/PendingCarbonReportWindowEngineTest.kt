package com.acme.transport.emissions.carbonreport.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCarbonReportWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCarbonReportWindowEngine()
        val item = newPendingCarbonReportWindow("code-525", "name-18")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
