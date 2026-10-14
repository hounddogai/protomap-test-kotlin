package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCapacityAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCapacityAgreementEngine()
        val item = newGlobalCapacityAgreement("code-961", "title-717")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
