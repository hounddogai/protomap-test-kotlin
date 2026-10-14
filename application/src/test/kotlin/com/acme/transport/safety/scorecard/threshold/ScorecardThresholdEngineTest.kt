package com.acme.transport.safety.scorecard.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardThresholdEngine()
        val item = newScorecardThreshold("code-447", "title-426")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
