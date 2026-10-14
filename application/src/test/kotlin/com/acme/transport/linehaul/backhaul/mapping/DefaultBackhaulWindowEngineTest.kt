package com.acme.transport.linehaul.backhaul.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultBackhaulWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultBackhaulWindowEngine()
        val item = newDefaultBackhaulWindow("code-795", "label-540")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
