package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCarbonReportRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCarbonReportRuleEngine(newRegistry())
        val item = newRegionalCarbonReportRule("code-795", "label-262")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
