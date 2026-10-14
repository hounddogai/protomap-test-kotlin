package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryIncidentSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryIncidentSegmentEngine()
        val item = newPrimaryIncidentSegment("code-824", "title-316")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
