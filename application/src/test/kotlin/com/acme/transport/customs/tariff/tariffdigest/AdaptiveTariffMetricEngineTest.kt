package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTariffMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTariffMetricEngine()
        val item = newAdaptiveTariffMetric("code-99", "name-448")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
