package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTariffPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTariffPolicyEngine()
        val item = newWeightedTariffPolicy("code-496", "name-128")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
