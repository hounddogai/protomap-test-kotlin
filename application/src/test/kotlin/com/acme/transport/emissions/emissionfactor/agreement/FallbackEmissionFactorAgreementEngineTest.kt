package com.acme.transport.emissions.emissionfactor.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackEmissionFactorAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackEmissionFactorAgreementEngine()
        val item = newFallbackEmissionFactorAgreement("code-130", "title-329")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
