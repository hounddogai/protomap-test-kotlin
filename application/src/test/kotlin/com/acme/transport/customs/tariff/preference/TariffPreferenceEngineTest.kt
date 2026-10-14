package com.acme.transport.customs.tariff.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffPreferenceEngine()
        val item = newTariffPreference("code-489", "name-883")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
