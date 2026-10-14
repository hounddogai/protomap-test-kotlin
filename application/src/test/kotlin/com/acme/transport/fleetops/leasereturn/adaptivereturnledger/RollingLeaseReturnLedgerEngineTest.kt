package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingLeaseReturnLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingLeaseReturnLedgerEngine()
        val item = newRollingLeaseReturnLedger("code-714", "label-497")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
