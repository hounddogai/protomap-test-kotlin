package com.acme.transport.network.transittime.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeAllocationEngine(newRegistry())
        val item = newTransitTimeAllocation("code-164", "name-245")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
