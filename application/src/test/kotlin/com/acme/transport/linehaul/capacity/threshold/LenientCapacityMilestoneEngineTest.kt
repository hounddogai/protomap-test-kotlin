package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCapacityMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCapacityMilestoneEngine(newRegistry())
        val item = newLenientCapacityMilestone("code-362", "label-574")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
