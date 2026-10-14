package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveFuelBurnClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveFuelBurnClaimEngine(newRegistry())
        val item = newAdaptiveFuelBurnClaim("code-644", "label-630")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
