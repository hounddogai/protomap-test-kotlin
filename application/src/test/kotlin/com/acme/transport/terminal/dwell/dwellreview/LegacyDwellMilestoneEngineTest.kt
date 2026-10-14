package com.acme.transport.terminal.dwell.dwellreview

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDwellMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDwellMilestoneEngine()
        val item = newLegacyDwellMilestone("code-448", "label-94")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
