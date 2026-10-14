package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRosterNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRosterNoticeEngine(newRegistry())
        val item = newCappedRosterNotice("code-285", "label-564")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
