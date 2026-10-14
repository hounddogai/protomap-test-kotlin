package com.acme.transport.network.zone.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalZoneReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalZoneReviewEngine(newRegistry())
        val item = newGlobalZoneReview("code-846", "title-116")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
