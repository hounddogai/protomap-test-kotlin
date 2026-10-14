package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTariffSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTariffSlotEngine()
        val item = newGlobalTariffSlot("code-819", "name-199")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
