package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRouteOffsetClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRouteOffsetClaimEngine()
        val item = newTieredRouteOffsetClaim("code-764", "title-679")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
