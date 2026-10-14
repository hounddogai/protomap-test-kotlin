package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDropMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDropMilestoneEngine()
        val item = newRollingDropMilestone("code-974", "label-463")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
