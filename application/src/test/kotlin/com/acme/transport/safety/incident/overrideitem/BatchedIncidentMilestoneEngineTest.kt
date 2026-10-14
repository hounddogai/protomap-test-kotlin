package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedIncidentMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedIncidentMilestoneEngine(newRegistry())
        val item = newBatchedIncidentMilestone("code-29", "title-552")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
