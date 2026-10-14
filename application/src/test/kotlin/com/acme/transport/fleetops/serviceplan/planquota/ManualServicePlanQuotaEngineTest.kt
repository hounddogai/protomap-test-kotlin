package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualServicePlanQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualServicePlanQuotaEngine(newRegistry())
        val item = newManualServicePlanQuota("code-790", "label-95")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
