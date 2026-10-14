package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRouteOffsetReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRouteOffsetReviewEngine(newRegistry())
        val item = newFallbackRouteOffsetReview("code-216", "name-934")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
