package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLeaseReturnRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLeaseReturnRollupEngine()
        val item = newCappedLeaseReturnRollup("code-713", "label-283")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
