package com.acme.transport.crewing.roster.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRosterRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRosterRuleEngine(newRegistry())
        val item = newStrictRosterRule("code-805", "title-445")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
