package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptReviewEngine(newRegistry())
        val item = newAttemptReview("code-831", "name-535")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
