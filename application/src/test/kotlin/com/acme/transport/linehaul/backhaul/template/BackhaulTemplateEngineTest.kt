package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulTemplateEngine()
        val item = newBackhaulTemplate("code-306", "name-224")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
