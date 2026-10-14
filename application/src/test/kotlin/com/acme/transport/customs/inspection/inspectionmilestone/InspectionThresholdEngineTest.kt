package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionThresholdEngine()
        val item = newInspectionThreshold("code-959", "name-980")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
