package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackAttemptMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackAttemptMilestoneEngine(newRegistry())
        val item = newFallbackAttemptMilestone("code-535", "label-870")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
