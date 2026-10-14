package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDefectThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDefectThresholdEngine()
        val item = newScheduledDefectThreshold("code-571", "name-44")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
