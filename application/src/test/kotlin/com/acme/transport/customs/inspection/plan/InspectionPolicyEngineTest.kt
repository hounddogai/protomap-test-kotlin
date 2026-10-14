package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionPolicyEngine(newRegistry())
        val item = newInspectionPolicy("code-880", "title-131")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
