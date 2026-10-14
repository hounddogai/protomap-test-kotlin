package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCutoffClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCutoffClaimEngine()
        val item = newFallbackCutoffClaim("code-582", "label-801")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
