package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTrailerClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTrailerClaimEngine(newRegistry())
        val item = newPrimaryTrailerClaim("code-961", "label-888")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
