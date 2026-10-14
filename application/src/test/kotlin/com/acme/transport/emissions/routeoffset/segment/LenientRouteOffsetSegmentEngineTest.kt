package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientRouteOffsetSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientRouteOffsetSegmentEngine(newRegistry())
        val item = newLenientRouteOffsetSegment("code-184", "name-654")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
