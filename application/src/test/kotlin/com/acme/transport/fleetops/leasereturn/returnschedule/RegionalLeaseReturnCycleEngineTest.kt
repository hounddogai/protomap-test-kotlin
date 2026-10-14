package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalLeaseReturnCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalLeaseReturnCycleEngine()
        val item = newRegionalLeaseReturnCycle("code-98", "label-905")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
