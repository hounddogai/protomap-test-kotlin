package com.acme.transport.safety.coaching.coachingsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingForecastEngine()
        val item = newCoachingForecast("code-737", "title-634")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
