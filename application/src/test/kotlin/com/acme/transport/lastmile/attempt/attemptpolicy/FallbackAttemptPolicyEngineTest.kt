package com.acme.transport.lastmile.attempt.attemptpolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackAttemptPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackAttemptPolicyEngine()
        val item = newFallbackAttemptPolicy("code-545", "name-565")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
