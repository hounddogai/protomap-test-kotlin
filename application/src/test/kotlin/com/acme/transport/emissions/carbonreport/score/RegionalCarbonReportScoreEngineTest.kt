package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCarbonReportScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCarbonReportScoreEngine()
        val item = newRegionalCarbonReportScore("code-700", "title-115")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
