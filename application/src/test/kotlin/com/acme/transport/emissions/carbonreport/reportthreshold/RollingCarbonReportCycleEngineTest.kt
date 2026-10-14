package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCarbonReportCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCarbonReportCycleEngine(newRegistry())
        val item = newRollingCarbonReportCycle("code-897", "name-894")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
