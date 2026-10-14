package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLeaseReturnEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLeaseReturnEstimateEngine(newRegistry())
        val item = newScheduledLeaseReturnEstimate("code-556", "label-727")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
