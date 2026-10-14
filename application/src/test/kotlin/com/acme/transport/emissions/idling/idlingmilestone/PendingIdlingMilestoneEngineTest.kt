package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingIdlingMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingIdlingMilestoneEngine(newRegistry())
        val item = newPendingIdlingMilestone("code-503", "title-137")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
