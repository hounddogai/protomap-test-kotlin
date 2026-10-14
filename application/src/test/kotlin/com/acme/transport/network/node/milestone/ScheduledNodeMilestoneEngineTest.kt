package com.acme.transport.network.node.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledNodeMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledNodeMilestoneEngine(newRegistry())
        val item = newScheduledNodeMilestone("code-260", "title-40")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
