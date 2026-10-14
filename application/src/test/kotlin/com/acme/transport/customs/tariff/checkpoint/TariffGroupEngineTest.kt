package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffGroupEngine()
        val item = newTariffGroup("code-431", "name-21")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
