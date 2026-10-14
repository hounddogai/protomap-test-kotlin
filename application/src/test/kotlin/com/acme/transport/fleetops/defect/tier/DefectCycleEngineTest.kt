package com.acme.transport.fleetops.defect.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectCycleEngine()
        val item = newDefectCycle("code-583", "label-222")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
