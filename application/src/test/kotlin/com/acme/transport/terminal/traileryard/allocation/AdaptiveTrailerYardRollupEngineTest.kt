package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTrailerYardRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTrailerYardRollupEngine()
        val item = newAdaptiveTrailerYardRollup("code-433", "label-946")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
