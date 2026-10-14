package com.acme.transport.fleetops.leasereturn.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnOverrideEngine()
        val item = newLeaseReturnOverride("code-976", "title-112")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
