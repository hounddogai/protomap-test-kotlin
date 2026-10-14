package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTrailerYardEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTrailerYardEntryEngine()
        val item = newWeightedTrailerYardEntry("code-454", "label-788")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
