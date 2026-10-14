package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryAvailabilityThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryAvailabilityThresholdEngine()
        val item = newPrimaryAvailabilityThreshold("code-930", "label-618")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
