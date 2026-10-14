package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardAdjustmentEngine()
        val item = newScorecardAdjustment("code-846", "title-496")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
