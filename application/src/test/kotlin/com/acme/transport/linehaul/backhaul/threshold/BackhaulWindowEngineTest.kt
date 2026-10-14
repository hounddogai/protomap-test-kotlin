package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulWindowEngine()
        val item = newBackhaulWindow("code-772", "title-367")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
