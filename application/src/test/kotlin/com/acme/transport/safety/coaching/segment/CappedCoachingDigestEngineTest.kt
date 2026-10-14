package com.acme.transport.safety.coaching.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCoachingDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCoachingDigestEngine()
        val item = newCappedCoachingDigest("code-444", "name-189")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
