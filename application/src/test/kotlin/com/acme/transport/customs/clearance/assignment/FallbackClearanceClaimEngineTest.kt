package com.acme.transport.customs.clearance.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackClearanceClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackClearanceClaimEngine()
        val item = newFallbackClearanceClaim("code-67", "label-934")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
