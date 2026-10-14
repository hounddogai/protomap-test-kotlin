package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnCheckpointEngine()
        val item = newLeaseReturnCheckpoint("code-869", "title-605")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
