package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetAllocationEngine()
        val item = newRouteOffsetAllocation("code-907", "title-69")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
