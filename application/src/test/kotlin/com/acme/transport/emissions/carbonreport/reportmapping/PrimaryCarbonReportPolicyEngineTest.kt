package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCarbonReportPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCarbonReportPolicyEngine(newRegistry())
        val item = newPrimaryCarbonReportPolicy("code-119", "name-867")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
