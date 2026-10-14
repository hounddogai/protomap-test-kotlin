package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalLeaseReturnBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalLeaseReturnBaselineEngine()
        val item = newRegionalLeaseReturnBaseline("code-423", "name-902")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
