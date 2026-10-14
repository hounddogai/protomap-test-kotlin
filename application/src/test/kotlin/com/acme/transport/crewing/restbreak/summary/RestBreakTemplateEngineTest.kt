package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakTemplateEngine(newRegistry())
        val item = newRestBreakTemplate("code-32", "name-652")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
