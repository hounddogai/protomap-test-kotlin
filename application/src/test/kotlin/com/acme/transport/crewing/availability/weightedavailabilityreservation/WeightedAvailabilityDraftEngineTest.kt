package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAvailabilityDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAvailabilityDraftEngine()
        val item = newWeightedAvailabilityDraft("code-828", "name-416")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
