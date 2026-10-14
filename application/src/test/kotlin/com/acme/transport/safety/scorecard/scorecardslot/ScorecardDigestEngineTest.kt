package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardDigestEngine(newRegistry())
        val item = newScorecardDigest("code-829", "label-947")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
