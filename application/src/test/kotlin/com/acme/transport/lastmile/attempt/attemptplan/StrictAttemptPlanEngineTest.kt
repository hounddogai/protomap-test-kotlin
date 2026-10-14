package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictAttemptPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictAttemptPlanEngine(newRegistry())
        val item = newStrictAttemptPlan("code-312", "title-159")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
