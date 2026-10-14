package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeAllocationEngine(newRegistry())
        val item = newEvChargeAllocation("code-265", "name-664")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
