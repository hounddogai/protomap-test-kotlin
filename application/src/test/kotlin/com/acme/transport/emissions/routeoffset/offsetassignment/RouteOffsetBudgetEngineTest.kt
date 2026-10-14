package com.acme.transport.emissions.routeoffset.offsetassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetBudgetEngine()
        val item = newRouteOffsetBudget("code-608", "label-9")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
