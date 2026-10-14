package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDefectAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDefectAssignmentEngine(newRegistry())
        val item = newFallbackDefectAssignment("code-211", "name-472")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
