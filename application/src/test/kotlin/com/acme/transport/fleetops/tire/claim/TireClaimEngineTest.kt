package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class TireClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireClaimEngine()
        val item = newTireClaim("code-992", "label-798")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
