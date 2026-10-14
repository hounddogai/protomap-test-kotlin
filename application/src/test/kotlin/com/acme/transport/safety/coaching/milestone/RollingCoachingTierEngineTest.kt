package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCoachingTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCoachingTierEngine()
        val item = newRollingCoachingTier("code-421", "name-498")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
