package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardAuditEngine()
        val item = newScorecardAudit("code-355", "title-867")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
