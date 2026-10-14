package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDefectPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDefectPlanEngine()
        val item = newDefaultDefectPlan("code-571", "title-602")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
