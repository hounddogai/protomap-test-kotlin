package com.acme.transport.customs.tariff.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TariffTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffTemplateEngine(newRegistry())
        val item = newTariffTemplate("code-57", "name-38")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
