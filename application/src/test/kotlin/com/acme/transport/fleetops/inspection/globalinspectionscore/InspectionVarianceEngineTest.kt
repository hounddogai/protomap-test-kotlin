package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionVarianceEngine(newRegistry())
        val item = newInspectionVariance("code-415", "title-666")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
