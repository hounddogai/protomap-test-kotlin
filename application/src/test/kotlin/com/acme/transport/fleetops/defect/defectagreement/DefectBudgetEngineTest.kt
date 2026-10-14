package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectBudgetEngine()
        val item = newDefectBudget("code-360", "label-649")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
