package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDefectAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDefectAgreementEngine()
        val item = newManualDefectAgreement("code-749", "title-743")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
