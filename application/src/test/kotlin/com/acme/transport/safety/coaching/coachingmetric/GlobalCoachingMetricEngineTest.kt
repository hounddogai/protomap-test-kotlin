package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCoachingMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCoachingMetricEngine(newRegistry())
        val item = newGlobalCoachingMetric("code-34", "name-316")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
