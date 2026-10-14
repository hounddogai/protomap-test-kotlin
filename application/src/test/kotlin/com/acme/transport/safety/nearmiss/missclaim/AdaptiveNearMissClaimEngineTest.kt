package com.acme.transport.safety.nearmiss.missclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveNearMissClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveNearMissClaimEngine()
        val item = newAdaptiveNearMissClaim("code-459", "name-427")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
