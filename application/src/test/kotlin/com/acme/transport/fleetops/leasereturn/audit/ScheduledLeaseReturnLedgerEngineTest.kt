package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLeaseReturnLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLeaseReturnLedgerEngine()
        val item = newScheduledLeaseReturnLedger("code-315", "label-916")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
