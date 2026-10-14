package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTrailerYardTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTrailerYardTemplateEngine()
        val item = newGlobalTrailerYardTemplate("code-347", "title-160")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
