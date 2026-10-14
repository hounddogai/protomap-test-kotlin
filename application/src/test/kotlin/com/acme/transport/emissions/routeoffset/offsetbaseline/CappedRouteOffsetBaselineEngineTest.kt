package com.acme.transport.emissions.routeoffset.offsetbaseline

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRouteOffsetBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRouteOffsetBaselineEngine()
        val item = newCappedRouteOffsetBaseline("code-183", "name-853")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
