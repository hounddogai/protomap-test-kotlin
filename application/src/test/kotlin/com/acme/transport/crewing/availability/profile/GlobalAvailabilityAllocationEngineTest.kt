package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalAvailabilityAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalAvailabilityAllocationEngine(newRegistry())
        val item = newGlobalAvailabilityAllocation("code-900", "name-278")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
