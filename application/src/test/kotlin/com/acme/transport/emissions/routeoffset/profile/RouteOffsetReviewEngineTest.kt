package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetReviewEngine()
        val item = newRouteOffsetReview("code-201", "name-363")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
