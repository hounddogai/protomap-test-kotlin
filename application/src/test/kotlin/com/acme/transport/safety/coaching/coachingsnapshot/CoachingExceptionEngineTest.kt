package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingExceptionEngine(newRegistry())
        val item = newCoachingException("code-335", "label-695")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
