package com.acme.transport.fleetops.inspection.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionClaimEngine()
        val item = newInspectionClaim("code-295", "title-807")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
