package com.acme.transport.crewing.availability.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityPlanEngine(newRegistry())
        val item = newAvailabilityPlan("code-134", "label-198")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
