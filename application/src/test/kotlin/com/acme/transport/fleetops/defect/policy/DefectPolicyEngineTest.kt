package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectPolicyEngine()
        val item = newDefectPolicy("code-424", "title-366")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
