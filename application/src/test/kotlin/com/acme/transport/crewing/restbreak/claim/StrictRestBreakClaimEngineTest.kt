package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRestBreakClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRestBreakClaimEngine(newRegistry())
        val item = newStrictRestBreakClaim("code-330", "title-662")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
