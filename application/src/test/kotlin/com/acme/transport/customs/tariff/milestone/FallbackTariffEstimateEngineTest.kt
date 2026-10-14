package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTariffEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTariffEstimateEngine()
        val item = newFallbackTariffEstimate("code-951", "title-621")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
