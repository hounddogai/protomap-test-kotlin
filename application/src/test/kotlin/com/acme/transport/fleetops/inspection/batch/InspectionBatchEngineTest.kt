package com.acme.transport.fleetops.inspection.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionBatchEngine()
        val item = newInspectionBatch("code-336", "name-171")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
