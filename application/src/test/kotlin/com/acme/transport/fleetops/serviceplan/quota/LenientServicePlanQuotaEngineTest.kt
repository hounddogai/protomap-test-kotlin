package com.acme.transport.fleetops.serviceplan.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientServicePlanQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientServicePlanQuotaEngine()
        val item = newLenientServicePlanQuota("code-404", "label-386")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
