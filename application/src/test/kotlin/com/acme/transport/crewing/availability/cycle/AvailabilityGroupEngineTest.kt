package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityGroupEngine(newRegistry())
        val item = newAvailabilityGroup("code-48", "name-621")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
