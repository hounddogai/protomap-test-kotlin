package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLockerAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLockerAssignmentEngine()
        val item = newCappedLockerAssignment("code-784", "title-313")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
