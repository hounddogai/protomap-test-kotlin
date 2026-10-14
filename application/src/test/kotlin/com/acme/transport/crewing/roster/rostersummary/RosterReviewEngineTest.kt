package com.acme.transport.crewing.roster.rostersummary

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterReviewEngine()
        val item = newRosterReview("code-644", "name-48")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
