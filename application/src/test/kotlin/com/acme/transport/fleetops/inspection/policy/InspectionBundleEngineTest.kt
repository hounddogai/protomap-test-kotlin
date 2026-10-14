package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionBundleEngine()
        val item = newInspectionBundle("code-630", "name-474")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
