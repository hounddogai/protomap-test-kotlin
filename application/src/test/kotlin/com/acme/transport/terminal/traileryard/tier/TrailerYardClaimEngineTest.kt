package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardClaimEngine()
        val item = newTrailerYardClaim("code-748", "name-33")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
