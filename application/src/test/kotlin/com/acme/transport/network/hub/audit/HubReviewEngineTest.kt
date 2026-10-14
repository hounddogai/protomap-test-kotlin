package com.acme.transport.network.hub.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubReviewEngine(newRegistry())
        val item = newHubReview("code-261", "title-27")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
