package com.acme.transport.fleetops.utilization.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedUtilizationAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedUtilizationAuditEngine()
        val item = newWeightedUtilizationAudit("code-589", "title-36")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
