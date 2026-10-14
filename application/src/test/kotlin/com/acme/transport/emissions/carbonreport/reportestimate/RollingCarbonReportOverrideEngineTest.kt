package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCarbonReportOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCarbonReportOverrideEngine(newRegistry())
        val item = newRollingCarbonReportOverride("code-765", "label-183")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
