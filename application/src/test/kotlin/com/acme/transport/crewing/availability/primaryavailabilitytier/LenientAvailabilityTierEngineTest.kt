package com.acme.transport.crewing.availability.primaryavailabilitytier

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientAvailabilityTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientAvailabilityTierEngine()
        val item = newLenientAvailabilityTier("code-745", "name-495")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
