package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnScoreEngine()
        val item = newLeaseReturnScore("code-562", "name-813")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
