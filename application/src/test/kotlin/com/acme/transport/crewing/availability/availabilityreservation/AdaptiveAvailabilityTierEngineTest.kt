package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAvailabilityTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAvailabilityTierEngine(newRegistry())
        val item = newAdaptiveAvailabilityTier("code-712", "title-988")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
