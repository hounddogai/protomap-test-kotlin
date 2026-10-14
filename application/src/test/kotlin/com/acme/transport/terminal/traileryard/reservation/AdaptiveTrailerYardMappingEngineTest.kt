package com.acme.transport.terminal.traileryard.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTrailerYardMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTrailerYardMappingEngine()
        val item = newAdaptiveTrailerYardMapping("code-478", "title-264")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
