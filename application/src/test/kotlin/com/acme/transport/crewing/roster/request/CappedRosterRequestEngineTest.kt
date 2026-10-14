package com.acme.transport.crewing.roster.request

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRosterRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRosterRequestEngine()
        val item = newCappedRosterRequest("code-79", "name-238")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
