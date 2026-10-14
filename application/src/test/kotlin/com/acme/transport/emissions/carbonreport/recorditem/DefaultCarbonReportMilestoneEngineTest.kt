package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCarbonReportMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCarbonReportMilestoneEngine(newRegistry())
        val item = newDefaultCarbonReportMilestone("code-819", "title-808")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
