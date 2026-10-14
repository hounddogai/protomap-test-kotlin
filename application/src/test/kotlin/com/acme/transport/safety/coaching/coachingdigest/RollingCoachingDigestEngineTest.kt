package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCoachingDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCoachingDigestEngine()
        val item = newRollingCoachingDigest("code-286", "label-118")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
