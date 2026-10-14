package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterDraftEngine(newRegistry())
        val item = newRosterDraft("code-993", "label-874")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
