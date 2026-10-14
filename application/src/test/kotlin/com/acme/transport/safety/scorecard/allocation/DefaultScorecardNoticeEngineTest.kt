package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultScorecardNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultScorecardNoticeEngine(newRegistry())
        val item = newDefaultScorecardNotice("code-616", "title-169")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
