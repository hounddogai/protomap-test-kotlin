package com.acme.transport.customs.tariff.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffEntryEngine()
        val item = newTariffEntry("code-226", "label-773")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
