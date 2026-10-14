package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionCycleEngine(newRegistry())
        val item = newInspectionCycle("code-566", "label-444")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
