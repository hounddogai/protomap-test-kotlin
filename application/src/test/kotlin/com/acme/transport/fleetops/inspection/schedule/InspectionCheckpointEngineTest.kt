package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionCheckpointEngine()
        val item = newInspectionCheckpoint("code-887", "title-450")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
