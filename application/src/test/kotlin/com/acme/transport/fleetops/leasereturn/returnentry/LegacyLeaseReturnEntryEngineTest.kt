package com.acme.transport.fleetops.leasereturn.returnentry

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLeaseReturnEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLeaseReturnEntryEngine()
        val item = newLegacyLeaseReturnEntry("code-605", "name-921")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
