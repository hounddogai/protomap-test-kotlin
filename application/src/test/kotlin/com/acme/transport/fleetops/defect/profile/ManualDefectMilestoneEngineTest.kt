package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDefectMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDefectMilestoneEngine()
        val item = newManualDefectMilestone("code-532", "name-728")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
