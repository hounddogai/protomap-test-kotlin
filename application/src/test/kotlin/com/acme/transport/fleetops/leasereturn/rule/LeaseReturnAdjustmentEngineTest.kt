package com.acme.transport.fleetops.leasereturn.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnAdjustmentEngine()
        val item = newLeaseReturnAdjustment("code-724", "name-467")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
