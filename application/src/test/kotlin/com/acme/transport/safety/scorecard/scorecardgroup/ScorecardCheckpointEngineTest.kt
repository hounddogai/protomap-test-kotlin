package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardCheckpointEngine(newRegistry())
        val item = newScorecardCheckpoint("code-221", "name-241")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
