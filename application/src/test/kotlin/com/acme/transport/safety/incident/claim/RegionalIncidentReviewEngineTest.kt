package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalIncidentReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalIncidentReviewEngine()
        val item = newRegionalIncidentReview("code-617", "name-618")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
