package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterRuleEngine()
        val item = newRosterRule("code-76", "title-251")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
