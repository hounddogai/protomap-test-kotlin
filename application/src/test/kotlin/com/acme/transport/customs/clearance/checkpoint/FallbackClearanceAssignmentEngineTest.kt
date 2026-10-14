package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackClearanceAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackClearanceAssignmentEngine(newRegistry())
        val item = newFallbackClearanceAssignment("code-746", "label-666")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
