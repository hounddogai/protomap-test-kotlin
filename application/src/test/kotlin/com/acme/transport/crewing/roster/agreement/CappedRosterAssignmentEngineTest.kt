package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRosterAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRosterAssignmentEngine(newRegistry())
        val item = newCappedRosterAssignment("code-657", "name-556")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
