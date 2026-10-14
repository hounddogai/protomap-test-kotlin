package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionWindowEngine()
        val item = newInspectionWindow("code-516", "name-968")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
