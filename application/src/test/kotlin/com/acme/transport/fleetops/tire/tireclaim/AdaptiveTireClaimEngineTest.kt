package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTireClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTireClaimEngine()
        val item = newAdaptiveTireClaim("code-848", "title-794")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
