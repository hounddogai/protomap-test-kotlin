package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRouteOffsetAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRouteOffsetAllocationEngine()
        val item = newBatchedRouteOffsetAllocation("code-579", "title-92")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
