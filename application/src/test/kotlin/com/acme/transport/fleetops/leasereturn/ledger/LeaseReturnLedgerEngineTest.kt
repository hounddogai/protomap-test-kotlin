package com.acme.transport.fleetops.leasereturn.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnLedgerEngine()
        val item = newLeaseReturnLedger("code-165", "title-425")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
