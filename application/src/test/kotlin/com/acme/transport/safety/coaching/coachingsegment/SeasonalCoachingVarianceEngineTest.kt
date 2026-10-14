package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCoachingVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCoachingVarianceEngine()
        val item = newSeasonalCoachingVariance("code-335", "label-154")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
