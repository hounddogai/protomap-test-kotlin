package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingAssignmentEngine(newRegistry())
        val item = newCoachingAssignment("code-700", "title-429")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
