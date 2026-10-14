package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLeaseReturnBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLeaseReturnBundleEngine(newRegistry())
        val item = newLegacyLeaseReturnBundle("code-53", "name-317")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
