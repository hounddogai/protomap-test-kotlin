package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTariffSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTariffSlotEngine(newRegistry())
        val item = newAdaptiveTariffSlot("code-674", "title-474")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
