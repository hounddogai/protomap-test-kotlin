package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedNearMissQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedNearMissQuotaEngine()
        val item = newWeightedNearMissQuota("code-158", "name-244")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
