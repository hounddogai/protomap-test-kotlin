package com.acme.transport.terminal.sortation.sortationdigest

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredSortationTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredSortationTemplateEngine()
        val item = newTieredSortationTemplate("code-439", "name-728")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
