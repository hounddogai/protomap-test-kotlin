package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardCycleEngine()
        val item = newTrailerYardCycle("code-437", "title-953")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
