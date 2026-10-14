package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationClaimEngine(newRegistry())
        val item = newUtilizationClaim("code-750", "name-193")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
