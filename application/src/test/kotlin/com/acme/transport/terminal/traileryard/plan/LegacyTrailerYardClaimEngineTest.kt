package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTrailerYardClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTrailerYardClaimEngine()
        val item = newLegacyTrailerYardClaim("code-961", "name-257")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
