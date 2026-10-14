package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionRuleEngine(newRegistry())
        val item = newInspectionRule("code-535", "label-796")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
