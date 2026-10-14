package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionSnapshotEngine()
        val item = newInspectionSnapshot("code-352", "label-302")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
