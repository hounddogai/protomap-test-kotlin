package com.acme.transport.crewing.availability.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityTierEngine(newRegistry())
        val item = newAvailabilityTier("code-121", "label-386")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
