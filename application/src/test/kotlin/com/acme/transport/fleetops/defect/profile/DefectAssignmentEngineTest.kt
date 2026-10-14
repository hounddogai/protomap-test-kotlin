package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectAssignmentEngine(newRegistry())
        val item = newDefectAssignment("code-957", "name-818")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
