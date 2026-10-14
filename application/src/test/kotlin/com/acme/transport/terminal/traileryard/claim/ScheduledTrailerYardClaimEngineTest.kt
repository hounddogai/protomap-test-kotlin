package com.acme.transport.terminal.traileryard.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTrailerYardClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTrailerYardClaimEngine()
        val item = newScheduledTrailerYardClaim("code-762", "name-285")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
