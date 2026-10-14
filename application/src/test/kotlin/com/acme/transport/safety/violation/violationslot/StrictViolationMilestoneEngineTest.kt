package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictViolationMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictViolationMilestoneEngine(newRegistry())
        val item = newStrictViolationMilestone("code-383", "name-96")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
