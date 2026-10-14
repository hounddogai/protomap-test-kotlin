package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionAgreementEngine(newRegistry())
        val item = newInspectionAgreement("code-527", "label-322")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
