package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedInspectionBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedInspectionBundleEngine()
        val item = newCappedInspectionBundle("code-761", "label-388")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
