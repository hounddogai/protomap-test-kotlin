package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalLeaseReturnAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalLeaseReturnAuditEngine()
        val item = newRegionalLeaseReturnAudit("code-692", "name-531")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
