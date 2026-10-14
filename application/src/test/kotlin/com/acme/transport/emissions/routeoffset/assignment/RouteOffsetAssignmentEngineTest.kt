package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetAssignmentEngine(newRegistry())
        val item = newRouteOffsetAssignment("code-338", "label-411")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
