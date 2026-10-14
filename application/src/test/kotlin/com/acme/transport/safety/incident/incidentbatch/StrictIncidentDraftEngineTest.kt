package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictIncidentDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictIncidentDraftEngine(newRegistry())
        val item = newStrictIncidentDraft("code-437", "name-980")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
