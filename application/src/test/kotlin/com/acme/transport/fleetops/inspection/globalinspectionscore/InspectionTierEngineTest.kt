package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionTierEngine()
        val item = newInspectionTier("code-38", "label-529")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
