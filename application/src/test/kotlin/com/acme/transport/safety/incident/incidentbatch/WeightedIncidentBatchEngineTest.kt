package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedIncidentBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedIncidentBatchEngine(newRegistry())
        val item = newWeightedIncidentBatch("code-451", "label-681")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
