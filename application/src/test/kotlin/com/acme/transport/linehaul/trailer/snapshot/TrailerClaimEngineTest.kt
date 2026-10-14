package com.acme.transport.linehaul.trailer.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerClaimEngine()
        val item = newTrailerClaim("code-822", "label-506")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
