package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffTierEngine()
        val item = newTariffTier("code-114", "title-101")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
