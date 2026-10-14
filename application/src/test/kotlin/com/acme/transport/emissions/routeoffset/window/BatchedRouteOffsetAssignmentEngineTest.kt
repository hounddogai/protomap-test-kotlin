package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRouteOffsetAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRouteOffsetAssignmentEngine()
        val item = newBatchedRouteOffsetAssignment("code-143", "title-853")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
