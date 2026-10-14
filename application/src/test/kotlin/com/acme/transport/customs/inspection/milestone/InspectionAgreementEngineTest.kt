package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionAgreementEngine()
        val item = newInspectionAgreement("code-60", "title-59")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
