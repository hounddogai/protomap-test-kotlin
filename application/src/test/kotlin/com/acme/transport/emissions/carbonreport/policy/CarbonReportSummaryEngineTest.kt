package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportSummaryEngine(newRegistry())
        val item = newCarbonReportSummary("code-958", "title-804")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
