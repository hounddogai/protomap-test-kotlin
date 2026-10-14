package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictLeaseReturnReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictLeaseReturnReservationEngine(newRegistry())
        val item = newStrictLeaseReturnReservation("code-87", "name-753")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
