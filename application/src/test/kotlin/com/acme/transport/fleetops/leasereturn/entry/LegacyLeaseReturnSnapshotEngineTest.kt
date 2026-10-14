package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLeaseReturnSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLeaseReturnSnapshotEngine(newRegistry())
        val item = newLegacyLeaseReturnSnapshot("code-180", "label-497")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
