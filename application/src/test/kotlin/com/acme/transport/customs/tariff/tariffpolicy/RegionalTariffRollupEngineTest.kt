package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTariffRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTariffRollupEngine()
        val item = newRegionalTariffRollup("code-406", "name-17")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
