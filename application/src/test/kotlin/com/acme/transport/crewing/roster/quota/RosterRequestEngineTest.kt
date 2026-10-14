package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterRequestEngine()
        val item = newRosterRequest("code-619", "label-235")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
