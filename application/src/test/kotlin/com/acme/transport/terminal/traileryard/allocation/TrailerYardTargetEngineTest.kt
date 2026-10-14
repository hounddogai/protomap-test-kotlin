package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardTargetEngine()
        val item = newTrailerYardTarget("code-665", "name-342")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
