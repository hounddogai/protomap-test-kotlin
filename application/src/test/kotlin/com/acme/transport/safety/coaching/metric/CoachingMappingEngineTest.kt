package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingMappingEngine()
        val item = newCoachingMapping("code-986", "title-211")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
