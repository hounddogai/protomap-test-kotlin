package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardGroupEngine()
        val item = newScorecardGroup("code-601", "name-240")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
