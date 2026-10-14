package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackInspectionPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackInspectionPolicyEngine(newRegistry())
        val item = newFallbackInspectionPolicy("code-3", "name-713")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
