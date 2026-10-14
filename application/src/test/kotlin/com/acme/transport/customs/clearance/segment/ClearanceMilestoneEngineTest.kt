package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceMilestoneEngine()
        val item = newClearanceMilestone("code-859", "label-531")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
