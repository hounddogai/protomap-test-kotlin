package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTariffAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTariffAgreementEngine()
        val item = newDefaultTariffAgreement("code-607", "name-747")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
