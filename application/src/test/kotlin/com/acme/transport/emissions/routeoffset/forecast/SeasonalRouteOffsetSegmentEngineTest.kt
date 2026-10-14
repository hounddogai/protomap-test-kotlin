package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRouteOffsetSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRouteOffsetSegmentEngine()
        val item = newSeasonalRouteOffsetSegment("code-157", "title-775")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
