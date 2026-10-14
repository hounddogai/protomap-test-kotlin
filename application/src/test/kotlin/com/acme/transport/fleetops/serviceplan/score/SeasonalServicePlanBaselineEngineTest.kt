package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalServicePlanBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalServicePlanBaselineEngine(newRegistry())
        val item = newSeasonalServicePlanBaseline("code-500", "label-54")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
