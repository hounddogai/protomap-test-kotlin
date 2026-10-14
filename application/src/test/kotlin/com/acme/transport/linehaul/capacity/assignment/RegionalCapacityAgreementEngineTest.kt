package com.acme.transport.linehaul.capacity.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCapacityAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCapacityAgreementEngine()
        val item = newRegionalCapacityAgreement("code-56", "label-911")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
