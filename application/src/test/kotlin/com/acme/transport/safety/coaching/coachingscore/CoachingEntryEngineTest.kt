package com.acme.transport.safety.coaching.coachingscore

import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingEntryEngine()
        val item = newCoachingEntry("code-687", "title-363")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
