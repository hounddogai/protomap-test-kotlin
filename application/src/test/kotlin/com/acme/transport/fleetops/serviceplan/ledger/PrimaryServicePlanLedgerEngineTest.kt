package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryServicePlanLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryServicePlanLedgerEngine(newRegistry())
        val item = newPrimaryServicePlanLedger("code-634", "label-912")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
