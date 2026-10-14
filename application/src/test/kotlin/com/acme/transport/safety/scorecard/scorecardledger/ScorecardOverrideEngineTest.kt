package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardOverrideEngine(newRegistry())
        val item = newScorecardOverride("code-476", "title-814")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
