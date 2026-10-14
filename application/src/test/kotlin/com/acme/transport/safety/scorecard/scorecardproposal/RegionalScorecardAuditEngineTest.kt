package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalScorecardAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalScorecardAuditEngine(newRegistry())
        val item = newRegionalScorecardAudit("code-129", "name-806")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
