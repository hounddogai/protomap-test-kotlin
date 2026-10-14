package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientBackhaulWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientBackhaulWindowEngine()
        val item = newLenientBackhaulWindow("code-983", "title-133")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
