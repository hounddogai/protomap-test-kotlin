package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockVarianceEngine()
        val item = newCrossDockVariance("code-98", "name-619")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
