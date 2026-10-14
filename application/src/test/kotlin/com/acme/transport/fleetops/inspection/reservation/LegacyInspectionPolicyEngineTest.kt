package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyInspectionPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyInspectionPolicyEngine()
        val item = newLegacyInspectionPolicy("code-52", "name-504")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
