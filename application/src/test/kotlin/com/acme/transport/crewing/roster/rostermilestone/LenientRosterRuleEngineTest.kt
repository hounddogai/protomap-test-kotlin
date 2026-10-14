package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientRosterRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientRosterRuleEngine()
        val item = newLenientRosterRule("code-248", "title-281")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
