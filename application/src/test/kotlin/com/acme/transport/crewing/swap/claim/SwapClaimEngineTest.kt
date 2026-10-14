package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapClaimEngine(newRegistry())
        val item = newSwapClaim("code-376", "title-42")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
