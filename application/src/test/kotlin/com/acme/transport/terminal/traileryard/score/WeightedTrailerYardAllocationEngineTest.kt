package com.acme.transport.terminal.traileryard.score

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTrailerYardAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTrailerYardAllocationEngine()
        val item = newWeightedTrailerYardAllocation("code-962", "title-477")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
