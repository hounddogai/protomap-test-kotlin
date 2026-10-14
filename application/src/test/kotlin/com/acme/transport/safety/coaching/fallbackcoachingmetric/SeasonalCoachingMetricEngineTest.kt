package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCoachingMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCoachingMetricEngine()
        val item = newSeasonalCoachingMetric("code-501", "label-269")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
