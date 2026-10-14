package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardPreferenceEngine(newRegistry())
        val item = newScorecardPreference("code-456", "title-363")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
