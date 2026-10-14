package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerTemplateEngine(newRegistry())
        val item = newTrailerTemplate("code-110", "title-971")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
