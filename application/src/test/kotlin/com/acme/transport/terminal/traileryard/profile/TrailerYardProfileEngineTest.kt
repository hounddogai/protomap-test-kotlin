package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardProfileEngine()
        val item = newTrailerYardProfile("code-410", "label-572")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
