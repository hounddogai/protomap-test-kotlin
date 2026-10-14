package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionEstimateEngine(newRegistry())
        val item = newInspectionEstimate("code-405", "title-178")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
