package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveScorecardCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveScorecardCheckpointEngine(newRegistry())
        val item = newAdaptiveScorecardCheckpoint("code-639", "title-667")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
