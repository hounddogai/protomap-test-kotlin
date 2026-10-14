package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingPolicyEngine()
        val item = newCoachingPolicy("code-455", "name-837")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
