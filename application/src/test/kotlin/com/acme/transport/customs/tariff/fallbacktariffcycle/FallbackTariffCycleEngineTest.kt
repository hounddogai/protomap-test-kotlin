package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTariffCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTariffCycleEngine()
        val item = newFallbackTariffCycle("code-939", "title-357")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
