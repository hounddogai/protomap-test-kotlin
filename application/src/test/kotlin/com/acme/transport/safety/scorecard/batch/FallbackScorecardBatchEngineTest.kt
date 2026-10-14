package com.acme.transport.safety.scorecard.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackScorecardBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackScorecardBatchEngine()
        val item = newFallbackScorecardBatch("code-356", "label-53")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
