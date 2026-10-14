package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceAssignmentEngine(newRegistry())
        val item = newClearanceAssignment("code-292", "title-671")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
