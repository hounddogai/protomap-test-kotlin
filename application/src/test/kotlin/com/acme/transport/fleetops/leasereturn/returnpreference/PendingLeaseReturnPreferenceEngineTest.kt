package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingLeaseReturnPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingLeaseReturnPreferenceEngine(newRegistry())
        val item = newPendingLeaseReturnPreference("code-402", "title-460")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
