package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTrailerYardScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTrailerYardScoreEngine()
        val item = newSeasonalTrailerYardScore("code-456", "title-798")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
