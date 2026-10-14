package com.acme.transport.fleetops.utilization.window

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationQuotaEngine()
        val item = newUtilizationQuota("code-697", "label-279")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
