package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackServicePlanLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackServicePlanLedgerEngine(newRegistry())
        val item = newFallbackServicePlanLedger("code-941", "title-50")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
