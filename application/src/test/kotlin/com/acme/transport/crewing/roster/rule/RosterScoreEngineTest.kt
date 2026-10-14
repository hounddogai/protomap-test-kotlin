package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterScoreEngine(newRegistry())
        val item = newRosterScore("code-786", "label-248")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
