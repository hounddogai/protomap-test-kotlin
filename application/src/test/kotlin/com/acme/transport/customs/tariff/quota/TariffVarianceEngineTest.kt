package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffVarianceEngine()
        val item = newTariffVariance("code-234", "label-571")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
