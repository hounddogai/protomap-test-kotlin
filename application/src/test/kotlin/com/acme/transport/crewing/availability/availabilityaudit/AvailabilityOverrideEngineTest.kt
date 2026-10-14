package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityOverrideEngine(newRegistry())
        val item = newAvailabilityOverride("code-851", "label-673")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
