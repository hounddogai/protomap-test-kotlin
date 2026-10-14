package com.acme.transport.fleetops.utilization.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualUtilizationClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualUtilizationClaimEngine(newRegistry())
        val item = newManualUtilizationClaim("code-692", "label-119")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
