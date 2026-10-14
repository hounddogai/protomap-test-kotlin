package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedScorecardBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedScorecardBundleEngine()
        val item = newBatchedScorecardBundle("code-67", "title-829")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
