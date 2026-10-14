package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalInspectionPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalInspectionPolicyEngine(newRegistry())
        val item = newGlobalInspectionPolicy("code-22", "name-510")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
