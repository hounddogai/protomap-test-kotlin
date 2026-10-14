package com.acme.transport.network.zone.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalZoneReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalZoneReviewEngine(newRegistry())
        val item = newSeasonalZoneReview("code-20", "name-183")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
