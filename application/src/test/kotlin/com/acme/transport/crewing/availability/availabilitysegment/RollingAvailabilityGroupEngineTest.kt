package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingAvailabilityGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingAvailabilityGroupEngine(newRegistry())
        val item = newRollingAvailabilityGroup("code-810", "title-593")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
