package com.acme.transport.safety.coaching.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingThresholdEngine()
        val item = newCoachingThreshold("code-303", "name-914")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
