package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTariffTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTariffTemplateEngine()
        val item = newTieredTariffTemplate("code-134", "title-510")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
