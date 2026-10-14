package com.acme.transport.lastmile.attempt.attempttier

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAttemptTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAttemptTierEngine()
        val item = newBatchedAttemptTier("code-932", "label-271")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
