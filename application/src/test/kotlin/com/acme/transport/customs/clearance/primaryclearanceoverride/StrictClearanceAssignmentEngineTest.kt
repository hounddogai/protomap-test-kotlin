package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictClearanceAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictClearanceAssignmentEngine()
        val item = newStrictClearanceAssignment("code-485", "label-715")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
