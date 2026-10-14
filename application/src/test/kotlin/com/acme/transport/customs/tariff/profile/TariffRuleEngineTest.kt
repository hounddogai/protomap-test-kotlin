package com.acme.transport.customs.tariff.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffRuleEngine()
        val item = newTariffRule("code-473", "title-518")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
