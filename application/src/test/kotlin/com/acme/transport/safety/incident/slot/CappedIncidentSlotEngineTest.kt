package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedIncidentSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedIncidentSlotEngine(newRegistry())
        val item = newCappedIncidentSlot("code-721", "title-290")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
