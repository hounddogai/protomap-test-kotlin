package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveSortationTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveSortationTemplateEngine()
        val item = newAdaptiveSortationTemplate("code-530", "title-272")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
