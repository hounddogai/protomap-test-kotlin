package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalProofOfDeliveryAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalProofOfDeliveryAllocationEngine(newRegistry())
        val item = newGlobalProofOfDeliveryAllocation("code-985", "name-775")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
