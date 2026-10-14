package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingBrokerAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingBrokerAllocationEngine(newRegistry())
        val item = newPendingBrokerAllocation("code-716", "name-127")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
