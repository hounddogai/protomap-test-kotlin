package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalUtilizationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalUtilizationPolicyEngine(newRegistry())
        val item = newSeasonalUtilizationPolicy("code-918", "name-372")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
