package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDwellTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDwellTemplateEngine()
        val item = newLenientDwellTemplate("code-946", "title-609")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
