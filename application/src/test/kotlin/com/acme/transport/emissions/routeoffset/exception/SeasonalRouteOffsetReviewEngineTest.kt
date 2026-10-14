package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRouteOffsetReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRouteOffsetReviewEngine(newRegistry())
        val item = newSeasonalRouteOffsetReview("code-113", "label-397")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
