package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingInspectionCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingInspectionCheckpointEngine()
        val item = newRollingInspectionCheckpoint("code-656", "name-299")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
