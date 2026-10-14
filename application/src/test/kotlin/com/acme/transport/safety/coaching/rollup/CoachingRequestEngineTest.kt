package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingRequestEngine(newRegistry())
        val item = newCoachingRequest("code-643", "title-208")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
