package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTrailerYardEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTrailerYardEstimateEngine()
        val item = newStrictTrailerYardEstimate("code-642", "name-354")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
