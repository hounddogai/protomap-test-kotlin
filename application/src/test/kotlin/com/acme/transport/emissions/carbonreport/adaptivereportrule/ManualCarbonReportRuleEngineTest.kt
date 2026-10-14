package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCarbonReportRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCarbonReportRuleEngine(newRegistry())
        val item = newManualCarbonReportRule("code-389", "label-60")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
