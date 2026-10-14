package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentReviewEngine(newRegistry())
        val item = newIncidentReview("code-977", "name-392")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
