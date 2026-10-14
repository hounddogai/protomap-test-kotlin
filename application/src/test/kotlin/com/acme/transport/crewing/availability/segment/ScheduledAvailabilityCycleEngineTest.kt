package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledAvailabilityCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledAvailabilityCycleEngine(newRegistry())
        val item = newScheduledAvailabilityCycle("code-697", "name-332")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
