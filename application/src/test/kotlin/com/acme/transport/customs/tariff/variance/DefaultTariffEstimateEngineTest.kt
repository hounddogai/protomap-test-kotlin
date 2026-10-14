package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTariffEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTariffEstimateEngine()
        val item = newDefaultTariffEstimate("code-179", "name-618")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
