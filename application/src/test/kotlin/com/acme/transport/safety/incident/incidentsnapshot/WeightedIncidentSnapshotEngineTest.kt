package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedIncidentSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedIncidentSnapshotEngine(newRegistry())
        val item = newWeightedIncidentSnapshot("code-505", "label-75")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
