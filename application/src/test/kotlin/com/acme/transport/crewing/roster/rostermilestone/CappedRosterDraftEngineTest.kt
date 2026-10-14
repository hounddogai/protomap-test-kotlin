package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRosterDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRosterDraftEngine()
        val item = newCappedRosterDraft("code-7", "label-171")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
