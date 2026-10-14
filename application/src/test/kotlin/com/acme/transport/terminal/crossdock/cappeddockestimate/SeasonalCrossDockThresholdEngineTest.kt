package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCrossDockThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCrossDockThresholdEngine()
        val item = newSeasonalCrossDockThreshold("code-662", "name-409")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
