package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCoachingDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCoachingDigestEngine()
        val item = newBatchedCoachingDigest("code-280", "name-628")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
