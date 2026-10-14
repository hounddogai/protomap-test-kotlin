package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRouteOffsetMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRouteOffsetMilestoneEngine()
        val item = newTieredRouteOffsetMilestone("code-583", "title-650")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
