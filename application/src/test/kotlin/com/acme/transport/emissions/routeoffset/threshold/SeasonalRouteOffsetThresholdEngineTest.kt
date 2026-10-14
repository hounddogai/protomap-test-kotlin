package com.acme.transport.emissions.routeoffset.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRouteOffsetThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRouteOffsetThresholdEngine()
        val item = newSeasonalRouteOffsetThreshold("code-777", "title-129")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
