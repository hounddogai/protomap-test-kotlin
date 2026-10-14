package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTariffRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTariffRuleEngine()
        val item = newSeasonalTariffRule("code-455", "title-576")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
