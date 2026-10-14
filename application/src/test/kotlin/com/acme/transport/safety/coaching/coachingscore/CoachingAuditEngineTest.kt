package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingAuditEngine(newRegistry())
        val item = newCoachingAudit("code-355", "title-427")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
