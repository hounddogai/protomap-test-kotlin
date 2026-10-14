package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityMilestoneEngine(newRegistry())
        val item = newAvailabilityMilestone("code-556", "title-440")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
