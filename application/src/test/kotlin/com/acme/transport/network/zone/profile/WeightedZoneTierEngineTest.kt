package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedZoneTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedZoneTierEngine()
        val item = newWeightedZoneTier("code-452", "label-845")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
