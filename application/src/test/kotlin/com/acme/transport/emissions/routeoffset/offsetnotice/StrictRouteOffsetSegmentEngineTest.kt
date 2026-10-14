package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRouteOffsetSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRouteOffsetSegmentEngine()
        val item = newStrictRouteOffsetSegment("code-673", "label-60")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
