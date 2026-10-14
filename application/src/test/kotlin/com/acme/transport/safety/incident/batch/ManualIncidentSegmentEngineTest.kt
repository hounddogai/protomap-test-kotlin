package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualIncidentSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualIncidentSegmentEngine(newRegistry())
        val item = newManualIncidentSegment("code-499", "label-30")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
