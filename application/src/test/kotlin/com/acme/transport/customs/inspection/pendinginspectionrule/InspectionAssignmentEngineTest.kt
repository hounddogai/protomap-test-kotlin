package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionAssignmentEngine(newRegistry())
        val item = newInspectionAssignment("code-374", "label-545")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
