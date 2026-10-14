package com.acme.transport.lastmile.attempt.attemptclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingAttemptTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingAttemptTierEngine()
        val item = newRollingAttemptTier("code-852", "label-294")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
