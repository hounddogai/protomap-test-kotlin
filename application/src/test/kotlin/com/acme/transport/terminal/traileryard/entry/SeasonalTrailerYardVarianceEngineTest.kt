package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTrailerYardVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTrailerYardVarianceEngine(newRegistry())
        val item = newSeasonalTrailerYardVariance("code-878", "name-676")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
