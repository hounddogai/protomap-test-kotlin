package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterClaimEngine()
        val item = newRosterClaim("code-494", "name-31")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
