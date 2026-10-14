package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterNoticeEngine(newRegistry())
        val item = newRosterNotice("code-219", "title-556")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
