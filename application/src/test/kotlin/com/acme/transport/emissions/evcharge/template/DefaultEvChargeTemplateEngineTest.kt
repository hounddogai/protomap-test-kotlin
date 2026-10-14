package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultEvChargeTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultEvChargeTemplateEngine()
        val item = newDefaultEvChargeTemplate("code-114", "title-171")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
