package com.acme.transport.safety.scorecard.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardNoticeEngine()
        val item = newScorecardNotice("code-537", "label-118")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
