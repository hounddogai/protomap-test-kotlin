package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultInspectionEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultInspectionEstimateEngine(newRegistry())
        val item = newDefaultInspectionEstimate("code-969", "name-678")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
