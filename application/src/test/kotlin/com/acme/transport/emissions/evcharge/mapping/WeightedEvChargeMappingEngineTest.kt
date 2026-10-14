package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedEvChargeMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedEvChargeMappingEngine()
        val item = newWeightedEvChargeMapping("code-85", "name-209")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
