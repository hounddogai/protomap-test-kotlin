package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingPreferenceEngine()
        val item = newCoachingPreference("code-134", "name-701")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
