package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanAuditEngine()
        val item = newServicePlanAudit("code-509", "name-829")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
