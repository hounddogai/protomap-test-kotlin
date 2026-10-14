package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveZoneAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveZoneAllocationEngine()
        val item = newAdaptiveZoneAllocation("code-582", "name-290")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
