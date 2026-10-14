package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLeaseReturnReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLeaseReturnReservationEngine()
        val item = newLegacyLeaseReturnReservation("code-680", "name-969")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
