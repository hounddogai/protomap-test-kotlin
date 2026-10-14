package com.acme.transport.safety.scorecard.window

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardBatchEngine()
        val item = newScorecardBatch("code-352", "title-767")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
