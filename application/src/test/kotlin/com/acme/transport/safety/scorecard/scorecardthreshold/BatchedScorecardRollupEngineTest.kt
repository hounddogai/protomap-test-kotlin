package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedScorecardRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedScorecardRollupEngine(newRegistry())
        val item = newBatchedScorecardRollup("code-296", "label-963")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
