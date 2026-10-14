package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationPolicyEngine(newRegistry())
        val item = newUtilizationPolicy("code-978", "title-719")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
