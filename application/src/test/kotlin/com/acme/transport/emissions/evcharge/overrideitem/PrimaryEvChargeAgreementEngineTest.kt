package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryEvChargeAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryEvChargeAgreementEngine()
        val item = newPrimaryEvChargeAgreement("code-584", "name-6")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
