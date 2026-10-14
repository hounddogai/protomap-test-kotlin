package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class DwellMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellMilestoneEngine()
        val item = newDwellMilestone("code-455", "label-905")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
