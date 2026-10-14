package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerTemplateEngine()
        val item = newManualTrailerTemplate("code-673", "title-331")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
