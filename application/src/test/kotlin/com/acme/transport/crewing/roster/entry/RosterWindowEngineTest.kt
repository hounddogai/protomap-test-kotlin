package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterWindowEngine(newRegistry())
        val item = newRosterWindow("code-524", "name-45")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
