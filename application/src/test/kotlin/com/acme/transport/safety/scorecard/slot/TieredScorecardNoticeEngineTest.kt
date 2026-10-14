package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredScorecardNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredScorecardNoticeEngine(newRegistry())
        val item = newTieredScorecardNotice("code-236", "title-931")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
