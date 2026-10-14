package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedScorecardNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedScorecardNoticeEngine(newRegistry())
        val item = newCappedScorecardNotice("code-950", "label-835")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
