package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualZoneAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualZoneAllocationEngine(newRegistry())
        val item = newManualZoneAllocation("code-245", "name-426")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
