package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityThresholdEngine(newRegistry())
        val item = newAvailabilityThreshold("code-197", "name-545")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
