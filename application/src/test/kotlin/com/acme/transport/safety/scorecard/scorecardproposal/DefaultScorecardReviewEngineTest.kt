package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultScorecardReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultScorecardReviewEngine(newRegistry())
        val item = newDefaultScorecardReview("code-557", "title-296")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
