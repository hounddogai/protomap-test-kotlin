package com.acme.transport.crewing.roster.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRosterRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRosterRequestEngine()
        val item = newStrictRosterRequest("code-945", "label-452")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
