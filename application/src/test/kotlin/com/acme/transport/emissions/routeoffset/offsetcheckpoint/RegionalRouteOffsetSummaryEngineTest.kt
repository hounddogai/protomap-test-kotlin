package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRouteOffsetSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRouteOffsetSummaryEngine()
        val item = newRegionalRouteOffsetSummary("code-180", "label-827")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
