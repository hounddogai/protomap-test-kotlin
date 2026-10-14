package com.acme.transport.emissions.fuelburn.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFuelBurnAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFuelBurnAgreementEngine()
        val item = newGlobalFuelBurnAgreement("code-230", "label-572")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
