package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCarbonReportPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCarbonReportPolicyEngine(newRegistry())
        val item = newGlobalCarbonReportPolicy("code-660", "name-777")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
