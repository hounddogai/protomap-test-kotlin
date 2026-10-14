package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingScorecardNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingScorecardNoticeEngine()
        val item = newRollingScorecardNotice("code-70", "title-425")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
