package com.acme.transport.safety.coaching.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCoachingPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCoachingPreferenceEngine()
        val item = newRollingCoachingPreference("code-646", "title-103")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
