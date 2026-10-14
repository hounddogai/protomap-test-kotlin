package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDefectClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDefectClaimEngine()
        val item = newPrimaryDefectClaim("code-401", "title-979")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
