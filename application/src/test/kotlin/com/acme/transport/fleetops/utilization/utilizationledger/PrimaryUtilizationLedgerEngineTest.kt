package com.acme.transport.fleetops.utilization.utilizationledger

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryUtilizationLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryUtilizationLedgerEngine()
        val item = newPrimaryUtilizationLedger("code-15", "label-843")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
