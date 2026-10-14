package com.acme.transport.customs.tariff.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTariffBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTariffBaselineEngine()
        val item = newFallbackTariffBaseline("code-485", "name-619")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
