package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportTierEngine(newRegistry())
        val item = newCarbonReportTier("code-661", "label-395")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
