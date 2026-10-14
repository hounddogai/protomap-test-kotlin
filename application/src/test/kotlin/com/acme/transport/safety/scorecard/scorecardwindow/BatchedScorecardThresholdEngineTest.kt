package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedScorecardThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedScorecardThresholdEngine()
        val item = newBatchedScorecardThreshold("code-987", "name-96")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
