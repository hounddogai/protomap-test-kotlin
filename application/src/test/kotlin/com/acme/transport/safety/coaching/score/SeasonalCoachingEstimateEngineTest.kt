package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCoachingEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCoachingEstimateEngine()
        val item = newSeasonalCoachingEstimate("code-479", "name-225")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
