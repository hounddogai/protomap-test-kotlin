package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTrailerYardScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTrailerYardScoreEngine()
        val item = newWeightedTrailerYardScore("code-352", "title-572")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
